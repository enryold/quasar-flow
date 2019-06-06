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
import it.enryold.quasarflow.enums.QMetricType;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.components.IAccumulator;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QEmitterList;
import it.enryold.quasarflow.models.metrics.FnBuildMetric;
import it.enryold.quasarflow.models.metrics.QMetric;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import it.enryold.quasarflow.models.utils.QSettings;
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


public abstract class AbstractProcessor<E> extends AbstractFlowable implements IProcessor<E> {

    private final Logger log = LoggerFactory.getLogger(getClass());


    final protected List<Fiber<Void>> subscriberStrands = new ArrayList<>();


    private Fiber<Void> dispatcherStrand;
    private Channel<E>[] rrChannels;
    final private List<ReceivePort<E>> processorChannels = new ArrayList<>();
    private IEmitter<E> emitter;
    private QRoutingKey routingKey;


    public AbstractProcessor(IEmitter<E> eEmitter, String name, QRoutingKey routingKey){
        this.flow = eEmitter.flow();
        this.emitter = eEmitter;
        this.settings = flow.getSettings();
        super.setName(name == null ? getClass().getSimpleName()+this.hashCode() : name);
        this.routingKey = routingKey == null ? QRoutingKey.broadcast() : routingKey;
        flow.addStartable(this);
    }

    public AbstractProcessor(IEmitter<E> eEmitter, QRoutingKey routingKey){
        this(eEmitter, null, routingKey);
    }

    public AbstractProcessor(IEmitter<E> eEmitter){
        this(eEmitter, null);
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        subscriberStrands
                .stream()
                .peek(s -> log("START Subscriber Strand "+s.getName()))
                .forEach(Fiber::start);

        if(dispatcherStrand != null){
            dispatcherStrand.start();
            log("START Dispatcher Strand "+dispatcherStrand.getName());
        }

    }

    @Override
    public IFlow flow() {
        return flow;
    }

    @Override
    public IFlowable parent() {
        return emitter;
    }


    protected  <T>ReceivePort<T> buildProcessor(Publisher<E> publisher, ITransformFactory<E, T> transformFactory)
    {
        final ITransform<E, T> transform = transformFactory.build();

        Processor<E, T> processor = ReactiveStreams.toProcessor(settings.getBufferSize(), settings.getOverflowPolicy(), (SuspendableAction2<ReceivePort<E>, SendPort<T>>) (in, out) -> {
            for (; ; ) {
                E x = in.receive();
                if (x == null)
                    break;
                receivedElements.incrementAndGet();

                T o = transform.apply(x);
                if(o != null){
                    out.send(o);
                }
            }
        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), processor);
    }


    protected ReceivePort<E> buildProcessor(Publisher<E> publisher)
    {
        final Processor<E, E> processor = ReactiveStreams.toProcessor(settings.getBufferSize(), settings.getOverflowPolicy(), (SuspendableAction2<ReceivePort<E>, SendPort<E>>) (in, out) -> {
            for (; ; ) {
                E x = in.receive();
                if (x == null)
                    break;
                receivedElements.incrementAndGet();

                out.send(x);
            }
        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), processor);
    }


    protected ReceivePort<List<E>> buildProcessorWithSizeBatching(Publisher<E> publisher,
                                                                int chunkSize,
                                                                int flushTimeout,
                                                                TimeUnit flushTimeUnit)
    {
        final Processor<E, List<E>> processor = ReactiveStreams.toProcessor(settings.getBufferSize(), settings.getOverflowPolicy(), (SuspendableAction2<ReceivePort<E>, SendPort<List<E>>>) (in, out) -> {
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

                    receivedElements.incrementAndGet();

                }while(collection.size() < chunkSize);

                if(collection.size() > 0){

                    out.send(new ArrayList<>(collection));
                }

                collection = new ArrayList<>();
            }


        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), processor);
    }



    protected <T>ReceivePort<List<T>> buildProcessorWithByteBatching(Publisher<E> publisher,
                                                                   IAccumulatorFactory<E, T> accumulatorFactory,
                                                                   int flushTimeout,
                                                                   TimeUnit flushTimeUnit)
    {
        final Processor<E, List<T>> processor = ReactiveStreams.toProcessor(settings.getBufferSize(), settings.getOverflowPolicy(), (SuspendableAction2<ReceivePort<E>, SendPort<List<T>>>) (in, out) -> {

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

                    receivedElements.incrementAndGet();

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
        return ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), processor);

    }


    private List<Publisher<E>> buildRRDispatcher(IEmitter<E> emitter, int workers)
    {
        this.emitter = emitter;

        rrChannels = IntStream.range(0, workers)
                .mapToObj(i -> Channels.<E>newChannel(settings.getBufferSize(), settings.getOverflowPolicy()))
                .toArray((IntFunction<Channel<E>[]>) Channel[]::new);


        final ReceivePort<E> roundRobinSubscriberChannel = ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), this.emitter.getPublisher(routingKey));
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


    private  <I>Fiber<Void> subscribeFiber(Channel<I> publisherChannel, ReceivePort<I> channel)
    {
        final IEmitterTask<I> task = this.buildEmitterTask(channel);
        return new Fiber<>((SuspendableRunnable) () -> task.emit(publisherChannel));
    }


    private <I>IEmitter<I> buildFanInEmitter(List<ReceivePort<I>> channels)
    {

        Channel<I> fanInChannel = Channels.newChannel(settings.getBufferSize(), settings.getOverflowPolicy());
        channels.stream()
                .map(ch -> subscribeFiber(fanInChannel, ch))
                .forEach(subscriberStrands::add);

        return new QEmitter<I>(flow)
                .broadcastEmitter(this.buildEmitterTask(fanInChannel));
    }

    protected <I> IEmitterTask<I> buildEmitterTask(ReceivePort<I> channel)
    {
        return publisherChannel -> {
            for (; ; ) {
                try {
                    I x = channel.receive();
                    if (x == null)
                        break;
                    producedElements.incrementAndGet();

                    publisherChannel.send(x);
                } catch (InterruptedException e) {
                    log("buildEmitterTask Strand interrupted: " + Strand.currentStrand().getName());
                } catch (Exception e) {
                    error("buildEmitterTask Strand in Exception: " + Strand.currentStrand().getName() + " - Message: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        };
    }



    public <EM extends IEmitter<E>> EM process(){
        ReceivePort<E> processor = this.buildProcessor(emitter.getPublisher(routingKey));
        this.registerProcessorChannel(processor);
        return (EM)new QEmitter<E>(flow, this.getName()+"Emitter").broadcastEmitter(buildEmitterTask(processor));
    }


    public <T, EM extends IEmitter<T>> EM process(ITransformFactory<E, T> transformFactory){
        ReceivePort<T> processor = this.buildProcessor(emitter.getPublisher(routingKey), transformFactory);
        this.registerProcessorChannel(processor);
        return (EM)new QEmitter<T>(flow, this.getName()+"Emitter").broadcastEmitter(buildEmitterTask(processor));
    }


    public IEmitterList<E> processWithFanOut(int workers){
        List<IEmitter<E>> emitters = buildRRDispatcher(emitter, workers)
                .stream()
                .map(this::buildProcessor)
                .peek(this::registerProcessorChannel)
                .map(this::buildEmitterTask)
                .map(task -> new QEmitter<E>(flow, this.getName()+"Emitter").broadcastEmitter(task))
                .map(e -> (IEmitter<E>)e)
                .collect(Collectors.toList());

        return new QEmitterList<>(emitters);

    }


    public <T> IEmitterList<T> processWithFanOut(int workers, ITransformFactory<E, T> transformFactory)
    {
        List<IEmitter<T>> emitters = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessor(p, transformFactory))
                .peek(this::registerProcessorChannel)
                .map(this::buildEmitterTask)
                .map(task -> new QEmitter<T>(flow, this.getName()+"Emitter").broadcastEmitter(task))
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
                .map(task -> new QEmitter<List<E>>(flow, this.getName()+"Emitter").broadcastEmitter(task))
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
                .map(task -> new QEmitter<List<T>>(flow, this.getName()+"Emitter").broadcastEmitter(task))
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
                                                           ITransformFactory<E, T> transformFactory){
        List<ReceivePort<T>> channels = buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessor(p, transformFactory))
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
            Stream.of(rrChannels).filter(s -> s != null && !s.isClosed()).forEach(SendPort::close);
        }

        subscriberStrands.stream().filter(Fiber::isAlive).forEach(s -> s.cancel(true));
        processorChannels.stream().filter(s -> s != null && !s.isClosed()).forEach(ReceivePort::close);
    }
}
