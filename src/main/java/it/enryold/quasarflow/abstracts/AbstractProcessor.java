package it.enryold.quasarflow.abstracts;


import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableAction2;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.ReceivePort;
import co.paralleluniverse.strands.channels.SendPort;
import co.paralleluniverse.strands.channels.reactivestreams.ReactiveStreams;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.components.IAccumulator;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QEmitterList;
import it.enryold.quasarflow.interfaces.*;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public abstract class AbstractProcessor<E> implements IProcessor<E> {

    private final Logger log = LoggerFactory.getLogger(getClass());



    private int subscribersBuffer = 1_000_000;
    private int processorsBuffer = 1_000_000;
    private int dispatcherBuffer = 1_000_000;
    private Channels.OverflowPolicy subscriberOverflowPolicy = Channels.OverflowPolicy.BLOCK;
    private Channels.OverflowPolicy processorOverflowPolicy = Channels.OverflowPolicy.BLOCK;
    private Channels.OverflowPolicy dispatcherOverflowPolicy = Channels.OverflowPolicy.BLOCK;

    private List<Fiber<Void>> subscriberStrands = new ArrayList<>();
    private Fiber<Void> dispatcherStrand;
    private Channel<E>[] rrChannels;
    private List<ReceivePort<E>> processorChannels = new ArrayList<>();
    private IEmitter<E> emitter;
    private String name;
    private String routingKey;
    private IFlow flow;


    public AbstractProcessor(IFlow flow, IEmitter<E> eEmitter, String name, String routingKey){
        this.flow = flow;
        this.emitter = eEmitter;
        this.name = name == null ? String.valueOf(this.hashCode()) : name;
        this.routingKey = routingKey;
        flow.addStartable(this);
    }

    public AbstractProcessor(IFlow flow, IEmitter<E> eEmitter, String routingKey){
        this(flow, eEmitter, null, routingKey);
    }

    public AbstractProcessor(IFlow flow, IEmitter<E> eEmitter){
        this(flow, eEmitter, null, "BROADCAST");
    }


    public <I extends IProcessor<E>> I withSubscriberBuffer(int buffer){
        this.subscribersBuffer = buffer;
        return (I)this;
    }

    public <I extends IProcessor<E>> I withSubscriberOverflowPolicy(Channels.OverflowPolicy overflowPolicy){
        this.subscriberOverflowPolicy = overflowPolicy;
        return (I)this;
    }

    public <I extends IProcessor<E>> I withProcessorsBuffer(int buffer){
        this.processorsBuffer = buffer;
        return (I)this;
    }

    public <I extends IProcessor<E>> I withProcessorOverflowPolicy(Channels.OverflowPolicy overflowPolicy){
        this.processorOverflowPolicy = overflowPolicy;
        return (I)this;
    }

    @Override
    public void start() {
        subscriberStrands.forEach(f -> {
            System.out.println("Start SUBSCRIBER "+name+" subscriber strand "+f.getName());
            f.start();
        });

        if(dispatcherStrand != null){
            System.out.println("Start SUBSCRIBER "+name+" dispatcher strand "+dispatcherStrand.getName());
            dispatcherStrand.start();
        }else{
            System.out.println("Start SUBSCRIBER "+name);
        }

    }

    @Override
    public IFlow flow() {
        return flow;
    }





    private <T>ReceivePort<T> buildProcessor(Publisher<E> publisher, ITransform<E, T> transform)
    {
        Processor<E, T> processor = ReactiveStreams.toProcessor(processorsBuffer, processorOverflowPolicy, (SuspendableAction2<ReceivePort<E>, SendPort<T>>) (in, out) -> {
            for (; ; ) {
                E x = in.receive();
                if (x == null)
                    break;
                T o = transform.apply(x);
                if(o != null){
                    out.send(o);
                }
            }
        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(subscribersBuffer, subscriberOverflowPolicy, processor);
    }


    private ReceivePort<E> buildProcessor(Publisher<E> publisher)
    {
        final Processor<E, E> processor = ReactiveStreams.toProcessor(processorsBuffer, processorOverflowPolicy, (SuspendableAction2<ReceivePort<E>, SendPort<E>>) (in, out) -> {
            for (; ; ) {
                E x = in.receive();
                if (x == null)
                    break;
                out.send(x);
            }
        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(subscribersBuffer, subscriberOverflowPolicy, processor);
    }


    private ReceivePort<List<E>> buildProcessorWithSizeBatching(Publisher<E> publisher,
                                                                int chunkSize,
                                                                int flushTimeout,
                                                                TimeUnit flushTimeUnit)
    {
        final Processor<E, List<E>> processor = ReactiveStreams.toProcessor(processorsBuffer, processorOverflowPolicy, (SuspendableAction2<ReceivePort<E>, SendPort<List<E>>>) (in, out) -> {
            List<E> collection = new ArrayList<>();

            for(;;){
                E x;
                long deadline = System.nanoTime() + flushTimeUnit.toNanos(flushTimeout);


                do{
                    x = in.receive(1, TimeUnit.NANOSECONDS);

                    if (x == null) { // not enough elements immediately available; will have to poll
                        x = in.receive(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                        if (x == null) {
                            break; // we already waited enough, and there are no more elements in sight
                        }
                        collection.add(x);
                    }else{
                        collection.add(x);
                    }

                }while(collection.size() < chunkSize);

                if(collection.size() > 0){
                    out.send(new ArrayList<>(collection));
                }

                collection = new ArrayList<>();
            }


        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(subscribersBuffer, subscriberOverflowPolicy, processor);
    }



    private <T>ReceivePort<List<T>> buildProcessorWithByteBatching(Publisher<E> publisher,
                                                                   IAccumulatorFactory<E, T> accumulatorFactory,
                                                                   int flushTimeout,
                                                                   TimeUnit flushTimeUnit)
    {
        final Processor<E, List<T>> processor = ReactiveStreams.toProcessor(processorsBuffer, processorOverflowPolicy, (SuspendableAction2<ReceivePort<E>, SendPort<List<T>>>) (in, out) -> {

            IAccumulator<E, T> accumulator = accumulatorFactory.build();


            for(;;){

                long deadline = System.nanoTime() + flushTimeUnit.toNanos(flushTimeout);
                boolean isAccumulatorAvailable;
                E elm;

                do{
                    elm = in.receive(1, TimeUnit.NANOSECONDS);

                    if (elm == null) { // not enough elements immediately available; will have to poll
                        elm = in.receive(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                        if (elm == null) {
                            break; // we already waited enough, and there are no more elements in sight
                        }
                        isAccumulatorAvailable = accumulator.add(elm);
                    }else{
                        isAccumulatorAvailable = accumulator.add(elm);
                    }
                }
                while (isAccumulatorAvailable);

                if(accumulator.getRecords().size() > 0){
                    out.send(accumulator.getRecords());
                }



                accumulator = accumulatorFactory.build();
                if(elm != null){ accumulator.add(elm); }
            }



        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(subscribersBuffer, subscriberOverflowPolicy, processor);

    }


    private List<Publisher<E>> buildRRDispatcher(IEmitter<E> emitter, int workers)
    {
        this.emitter = emitter;

        rrChannels = IntStream.range(0, workers)
                .mapToObj(i -> Channels.<E>newChannel(dispatcherBuffer, dispatcherOverflowPolicy))
                .toArray((IntFunction<Channel<E>[]>) Channel[]::new);


        final ReceivePort<E> roundRobinSubscriberChannel = ReactiveStreams.subscribe(subscribersBuffer, subscriberOverflowPolicy, this.emitter.getPublisher(routingKey));
        dispatcherStrand = new Fiber<>((SuspendableRunnable) () -> {

            int index = 0;


            for (; ; ) {
                E x = roundRobinSubscriberChannel.receive();
                if (x != null){
                    rrChannels[index++].send(x);
                    if (index == workers)
                        index = 0;
                }
            }
        });

        return Stream.of(rrChannels).map(ReactiveStreams::toPublisher).collect(Collectors.toList());

    }

    private void registerProcessorChannel(ReceivePort receivePort){
        processorChannels.add(receivePort);
    }


    private <I>Fiber<Void> subscribeFiber(Channel<I> publisherChannel, ReceivePort<I> channel)
    {
        return new Fiber<>((SuspendableRunnable) () -> {
            for (; ; ) {
                try{
                    I x = channel.receive();
                    if (x == null)
                        break;

                    publisherChannel.send(x);

                }
                catch (InterruptedException e){
                    log.debug("Strand interrupted: "+Strand.currentStrand().getName());
                }
                catch (Exception e){
                    log.error("Strand in Exception: "+Strand.currentStrand().getName()+" - Message: "+e.getMessage());
                    e.printStackTrace();
                }

            }
        });
    }


    private <I>IEmitter<I> buildFanInEmitter(List<ReceivePort<I>> channels)
    {

        Channel<I> fanInChannel = Channels.newChannel(processorsBuffer, processorOverflowPolicy);
        channels.stream()
                .map(ch -> subscribeFiber(fanInChannel, ch))
                .forEach(subscriberStrands::add);

        return new QEmitter<I>(flow)
                .broadcastEmitter(publisherChannel -> {
                    for(;;){ publisherChannel.send(fanInChannel.receive()); }
                });
    }

    private <I> IEmitterTask<I> buildEmitterTask(ReceivePort<I> channel)
    {
        return publisherChannel -> {
            for (; ; ) {
                try {
                    I x = channel.receive();
                    if (x == null)
                        break;

                    publisherChannel.send(x);
                } catch (InterruptedException e) {
                    log.debug("Strand interrupted: " + Strand.currentStrand().getName());
                } catch (Exception e) {
                    log.error("Strand in Exception: " + Strand.currentStrand().getName() + " - Message: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        };
    }



    public <EM extends IEmitter<E>> EM process(){
        ReceivePort<E> processor = this.buildProcessor(emitter.getPublisher(routingKey));
        this.registerProcessorChannel(processor);
        return (EM)new QEmitter<E>(flow).broadcastEmitter(buildEmitterTask(processor));
    }


    public <T, EM extends IEmitter<T>> EM process(ITransform<E, T> transform){
        ReceivePort<T> processor = this.buildProcessor(emitter.getPublisher(routingKey), transform);
        this.registerProcessorChannel(processor);
        return (EM)new QEmitter<T>(flow).broadcastEmitter(buildEmitterTask(processor));
    }


    public IEmitterList<E> processWithFanOut(int workers){
        List<IEmitter<E>> emitters = buildRRDispatcher(emitter, workers)
                .stream()
                .map(this::buildProcessor)
                .peek(this::registerProcessorChannel)
                .map(this::buildEmitterTask)
                .map(task -> new QEmitter<E>(flow).broadcastEmitter(task))
                .map(e -> (IEmitter<E>)e)
                .collect(Collectors.toList());

        return new QEmitterList<>(emitters);

    }


    public <T> IEmitterList<T> processWithFanOut(int workers,
                                                                  ITransform<E, T> transform)
    {
        List<IEmitter<T>> emitters = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessor(p, transform))
                .peek(this::registerProcessorChannel)
                .map(this::buildEmitterTask)
                .map(task -> new QEmitter<T>(flow).broadcastEmitter(task))
                .map(e -> (IEmitter<T>)e)
                .collect(Collectors.toList());

        return new QEmitterList<>(emitters);

    }

    public IEmitterList<List<E>> processWithFanOutAndSizeBatching(
            int workers,
            int chunkSize,
            int flushTimeout,
            TimeUnit flushTimeUnit)
    {
        List<IEmitter<List<E>>> emitters = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessorWithSizeBatching(p, chunkSize, flushTimeout, flushTimeUnit))
                .peek(this::registerProcessorChannel)
                .map(this::buildEmitterTask)
                .map(task -> new QEmitter<List<E>>(flow).broadcastEmitter(task))
                .map(e -> (IEmitter<List<E>>)e)
                .collect(Collectors.toList());

        return new QEmitterList<>(emitters);

    }


    public <T> IEmitterList<List<T>> processWithFanOutAndByteBatching(
            int workers,
            IAccumulatorFactory<E, T> accumulatorFactory,
            int flushTimeout,
            TimeUnit flushTimeUnit)
    {
        List<IEmitter<List<T>>> emitters = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessorWithByteBatching(p, accumulatorFactory, flushTimeout, flushTimeUnit))
                .peek(this::registerProcessorChannel)
                .map(this::buildEmitterTask)
                .map(task -> new QEmitter<List<T>>(flow).broadcastEmitter(task))
                .map(e -> (IEmitter<List<T>>)e)
                .collect(Collectors.toList());

        return new QEmitterList<>(emitters);

    }


    public <EM extends IEmitter<E>> EM processWithFanIn(int workers){
        List<ReceivePort<E>> channels = buildRRDispatcher(emitter, workers)
                .stream()
                .map(this::buildProcessor)
                .peek(this::registerProcessorChannel)
                .collect(Collectors.toList());

       return (EM)buildFanInEmitter(channels);
    }


    public <T, EM extends IEmitter<T>> EM processWithFanIn(int workers,
                                                           ITransform<E, T> transform){
        List<ReceivePort<T>> channels = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessor(p, transform))
                .peek(this::registerProcessorChannel)
                .collect(Collectors.toList());

        return (EM)buildFanInEmitter(channels);


    }

    public <EM extends IEmitter<List<E>>> EM processWithFanInAndSizeBatching(
            int workers,
            int chunkSize,
            int flushTimeout,
            TimeUnit flushTimeUnit){

        List<ReceivePort<List<E>>> channels = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessorWithSizeBatching(p, chunkSize, flushTimeout, flushTimeUnit))
                .peek(this::registerProcessorChannel)
                .collect(Collectors.toList());

        return (EM)buildFanInEmitter(channels);

    }

    public <T, EM extends IEmitter<List<T>>> EM processWithFanInAndByteBatching(
            int workers,
            IAccumulatorFactory<E, T> accumulatorFactory,
            int flushTimeout,
            TimeUnit flushTimeUnit){

        List<ReceivePort<List<T>>> tasks = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessorWithByteBatching(p, accumulatorFactory, flushTimeout, flushTimeUnit))
                .peek(this::registerProcessorChannel)
                .collect(Collectors.toList());

        return (EM)buildFanInEmitter(tasks);
    }


    @Override
    public String toString() {
        return "SUBSCRIBER: "+((name == null) ? this.hashCode() : name);
    }

    @Override
    public void destroy() {
        if(dispatcherStrand != null){
            dispatcherStrand.cancel(true);
            Stream.of(rrChannels).forEach(SendPort::close);
        }

        subscriberStrands.stream().filter(Fiber::isAlive).forEach(s -> s.cancel(true));
        processorChannels.forEach(ReceivePort::close);
    }
}
