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
import it.enryold.quasarflow.components.IAccumulator;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.enums.QMetricType;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.utils.FnBuildMetric;
import it.enryold.quasarflow.models.utils.QMetric;
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


public abstract class AbstractConsumer<E> implements IConsumer<E> {

    private final Logger log = LoggerFactory.getLogger(getClass());


    private List<Fiber<Void>> subscriberStrands = new ArrayList<>();
    private List<ReceivePort> processorChannels = new ArrayList<>();
    private Fiber<Void> dispatcherStrand;
    private IEmitter<E> emitter;
    private Channel<E>[] rrChannels;
    private Channel<QMetric> metricChannel;
    private String name;
    private IFlow flow;
    private QSettings settings;


    public AbstractConsumer(IEmitter<E> eEmitter, String name){
        this.flow = eEmitter.flow();
        this.emitter = eEmitter;
        this.settings = flow.getSettings();
        this.name = name == null ? getClass().getSimpleName()+this.hashCode() : name;
        flow.addStartable(this);
    }


    public AbstractConsumer(IEmitter<E> eEmitter){
        this(eEmitter, null);
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <I extends IFlowable<E>> I withMetricChannel(Channel<QMetric> metricChannel) {
        this.metricChannel = metricChannel;
        return (I)this;
    }
    @Override
    public void start() {
        subscriberStrands.forEach(Fiber::start);

        if(dispatcherStrand != null){
            dispatcherStrand.start();
        }
    }

    @Override
    public IFlow flow() {
        return flow;
    }




    private ReceivePort<E> buildProcessor()
    {
        Publisher<E> publisher = emitter.getPublisher();

        Processor<E, E> processor = ReactiveStreams.toProcessor(settings.getBufferSize(), settings.getOverflowPolicy(), (SuspendableAction2<ReceivePort<E>, SendPort<E>>) (in, out) -> {
            for (; ; ) {
                E x = in.receive();
                if (x == null)
                    break;

                if(metricChannel != null) {
                    metricChannel.trySend(new FnBuildMetric().apply(this, QMetricType.RECEIVED.name()));
                }
                out.send(x);
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


        final ReceivePort<E> roundRobinSubscriberChannel = ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), this.emitter.getPublisher());
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

    private ReceivePort<List<E>> buildProcessorWithSizeBatching(Publisher<E> publisher,
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

                }while(collection.size() < chunkSize);

                if(collection.size() > 0){
                    if(metricChannel != null) {
                        metricChannel.trySend(new FnBuildMetric().apply(this, QMetricType.RECEIVED.name()));
                    }
                    out.send(new ArrayList<>(collection));
                }

                collection = new ArrayList<>();

            }


        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), processor);
    }



    private <T>ReceivePort<List<T>> buildProcessorWithByteBatching(Publisher<E> publisher,
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
                }
                while (isAccumulatorAvailable);

                if(accumulator.getRecords().size() > 0){
                    out.send(new ArrayList<>(accumulator.getRecords()));
                    if(metricChannel != null) {
                        metricChannel.trySend(new FnBuildMetric().apply(this, QMetricType.RECEIVED.name()));
                    }
                }

                accumulator = accumulatorFactory.build();
                if(elm != null){ accumulator.add(elm); }
            }



        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), processor);

    }

    private <I>Fiber<Void> subscribeFiber(ReceivePort<I> channel, IConsumerTask<I> ingestionTask)
    {
        return new Fiber<>((SuspendableRunnable) () -> {
            for (; ; ) {
                try{
                    I x = channel.receive();
                    if (x == null)
                        break;

                    if(metricChannel != null) {
                        metricChannel.trySend(new FnBuildMetric().apply(this, QMetricType.PRODUCED.name()));
                    }
                    ingestionTask.ingest(x);

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


    private <T>void registerProcessorChannel(ReceivePort<T> receivePort){
        processorChannels.add(receivePort);
    }

    @Override
    public IFlow consume(IConsumerTask<E> task) {

        ReceivePort<E> processor = buildProcessor();
        this.registerProcessorChannel(processor);
        subscriberStrands.add(subscribeFiber(processor, task));
        return this.flow;
    }


    @Override
    public IFlow consumeWithSizeBatching(int chunkSize, int flushTimeout, TimeUnit flushTimeUnit, IConsumerTask<List<E>> task){

        ReceivePort<List<E>> processor = buildProcessorWithSizeBatching(emitter.getPublisher(), chunkSize, flushTimeout, flushTimeUnit);
        this.registerProcessorChannel(processor);
        subscriberStrands.add(subscribeFiber(processor, task));
        return this.flow;

    }

    @Override
    public <T>IFlow consumeWithByteBatching(
            IAccumulatorFactory<E, T> accumulatorFactory,
            int flushTimeout,
            TimeUnit flushTimeUnit,
            IConsumerTask<List<T>> task){

        ReceivePort<List<T>> processor = buildProcessorWithByteBatching(emitter.getPublisher(), accumulatorFactory, flushTimeout, flushTimeUnit);
        this.registerProcessorChannel(processor);
        subscriberStrands.add(subscribeFiber(processor, task));
        return this.flow;
    }


    @Override
    public IFlow consumeWithFanOutAndSizeBatching(int workers,
                                                  int chunkSize,
                                                  int flushTimeout,
                                                  TimeUnit flushTimeUnit,
                                                  IConsumerTaskFactory<List<E>> taskFactory){

        buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessorWithSizeBatching(p, chunkSize, flushTimeout, flushTimeUnit))
                .peek(this::registerProcessorChannel)
                .forEach(c -> subscriberStrands.add(subscribeFiber(c, taskFactory.build())));

        return this.flow;

    }

    @Override
    public <T>IFlow consumeWithFanOutAndByteBatching(int workers,
                                                     IAccumulatorFactory<E, T> accumulatorFactory,
                                                     int flushTimeout,
                                                     TimeUnit flushTimeUnit,
                                                     IConsumerTaskFactory<List<T>> taskFactory){

        buildRRDispatcher(emitter, workers)
                .stream()
                .map(p -> buildProcessorWithByteBatching(p, accumulatorFactory, flushTimeout, flushTimeUnit))
                .peek(this::registerProcessorChannel)
                .forEach(c -> subscriberStrands.add(subscribeFiber(c, taskFactory.build())));

        return this.flow;
    }



    @Override
    public String toString() {
        return "RECEIVER: "+((name == null) ? this.hashCode() : name);
    }

    @Override
    public void destroy() {
        subscriberStrands.stream().filter(Fiber::isAlive).forEach(s -> s.cancel(true));
        processorChannels.stream().filter(s -> s != null && !s.isClosed()).forEach(ReceivePort::close);
    }
}
