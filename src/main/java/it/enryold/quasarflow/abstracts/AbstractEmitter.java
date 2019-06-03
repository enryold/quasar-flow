package it.enryold.quasarflow.abstracts;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.*;
import co.paralleluniverse.strands.channels.reactivestreams.ReactiveStreams;
import it.enryold.quasarflow.enums.QMetricType;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.metrics.FnBuildMetric;
import it.enryold.quasarflow.models.metrics.QMetric;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import it.enryold.quasarflow.models.utils.QSettings;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEmitter<T> implements IEmitter<T> {

    protected Channel<T> emitterTaskChannel;
    private Fiber emitterTaskStrand;
    private Publisher<T> emitterTaskPublisher;
    final private Map<String, List<Channel<T>>> channels = new HashMap<>();
    private Channel<QMetric> metricChannel;
    private IEmitterTask<T> task;
    private QSettings settings;
    private Fiber dispatcher;
    private String name;
    private IRoutingKeyExtractor<T> extractorFunction;
    protected IFlow flow;


    public AbstractEmitter(IFlow flow){
        this(flow, null);
    }

    public AbstractEmitter(IFlow flow, String name){
        this.flow = flow;
        this.settings = flow.getSettings();
        this.name = name == null ? getClass().getSimpleName()+this.hashCode() : name;
        flow.addStartable(this);
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
    public <I extends IFlowable<T>> I withMetricChannel(Channel<QMetric> metricChannel) {
        this.metricChannel = metricChannel;
        return (I)this;
    }

    public <E extends IEmitter<T>> E broadcast(){
        return this.broadcastEmitter(task);
    }

    public <E extends IEmitter<T>> E routed(IRoutingKeyExtractor<T> extractor){
        return this.routedEmitter(task, extractor);
    }


    public <E extends IEmitter<T>> E broadcastEmitter(IEmitterTask<T> task)
    {
        this.task = task;
        emitterTaskChannel = Channels.newChannel(settings.getBufferSize(), settings.getOverflowPolicy());
        emitterTaskStrand = new Fiber<Void>((SuspendableRunnable) () -> { if(task != null){
            if(metricChannel != null){
                metricChannel.send(new FnBuildMetric().create(this, QMetricType.PRODUCED.name(), 1L));
            }
            task.emit(emitterTaskChannel);
        }
        });
        emitterTaskPublisher = ReactiveStreams.toPublisher(emitterTaskChannel);
        return (E)this;
    }

    @Override
    public <E extends IEmitter<T>> E routedEmitter(IEmitterTask<T> task, IRoutingKeyExtractor<T> extractor) {
        this.task = task;
        this.extractorFunction = extractor;
        emitterTaskChannel = Channels.newChannel(settings.getBufferSize(), settings.getOverflowPolicy());
        emitterTaskStrand = new Fiber<Void>((SuspendableRunnable) () -> { if(task != null){
            if(metricChannel != null){
                metricChannel.send(new FnBuildMetric().create(this, QMetricType.PRODUCED.name(), 1L));
            }
            task.emit(emitterTaskChannel);
        } });
        emitterTaskPublisher = ReactiveStreams.toPublisher(emitterTaskChannel);
        return (E)this;
    }


    @Override
    public <S extends IProcessor<T>> S addProcessor(String name) {
        S processor = addProcessor();
        processor.setName(name);
        return processor;
    }

    @Override
    public IEmitter<T> addProcessor(Injector<IProcessor<T>> processorInjector){
        IEmitter<T> emitter = currentInstance();
        IProcessor<T> processor = addProcessor();
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public IEmitter<T> addProcessor(String name, Injector<IProcessor<T>> processorInjector){
        IEmitter<T> emitter = currentInstance();
        IProcessor<T> processor = addProcessor(name);
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public IEmitter<T> addProcessor(QRoutingKey routingKey, Injector<IProcessor<T>> processorInjector){
        IEmitter<T> emitter = currentInstance();
        IProcessor<T> processor = addProcessor(routingKey);
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public IEmitter<T> addProcessor(String name, QRoutingKey routingKey, Injector<IProcessor<T>> processorInjector){
        IEmitter<T> emitter = currentInstance();
        IProcessor<T> processor = addProcessor(name, routingKey);
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public IEmitter<T> addConsumer(Injector<IConsumer<T>> consumerInjector) {
        IEmitter<T> emitter = currentInstance();
        IConsumer<T> consumer = addConsumer();
        consumerInjector.accept(consumer);
        return emitter;
    }

    @Override
    public IEmitter<T> addConsumer(String name, Injector<IConsumer<T>> consumerInjector) {
        IEmitter<T> emitter = currentInstance();
        IConsumer<T> consumer = addConsumer(name);
        consumerInjector.accept(consumer);
        return emitter;
    }

    @Override
    public <S> IEmitter<S> map(InjectorEmitter<T, S> injector){
        return injector.injectEmitter(currentInstance());
    }


    @Override
    public IEmitter<T> consume(InjectorConsumer<T> injector) {
        IEmitter<T> emitter = currentInstance();
        injector.injectConsumer(emitter);
        return emitter;
    }

    @Override
    public <S extends IConsumer<T>> S addConsumer(String name) {
        S consumer = addConsumer();
        consumer.setName(name);
        return consumer;
    }




    @Override
    public Channel<T> getChannel() {
        return emitterTaskChannel;
    }

    @Override
    public void start() {

        if(extractorFunction == null){
            buildBroadcaster();
        }else{
            buildRouted();
        }

        dispatcher.start();
        emitterTaskStrand.start();
    }

    @Override
    public IFlow flow() {
        return flow;
    }



    @Override
    public void destroy() {
        emitterTaskStrand.cancel(true);
        emitterTaskChannel.close();
        channels.entrySet()
                .stream()
                .flatMap(s -> s.getValue().stream())
                .filter(s -> s != null && !s.isClosed())
                .forEach(SendPort::close);
    }

    private void buildBroadcaster()
    {
        ReceivePort<T> receiver = ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), emitterTaskPublisher);
        final List<Channel<T>> channelList = channels.entrySet()
                .stream()
                .flatMap(s -> s.getValue().stream())
                .collect(Collectors.toList());

        dispatcher = new Fiber<>((SuspendableRunnable) () -> {

            for (; ; ) {
                T x = receiver.receive();
                if(x == null){
                    break;
                }
                for(Channel<T> ch : channelList){
                    try {
                        ch.send(x);
                    } catch (SuspendExecution | InterruptedException suspendExecution) {
                        suspendExecution.printStackTrace();
                    }
                }
            }
        });
    }


    private void buildRouted()
    {
        ReceivePort<T> receiver = ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), emitterTaskPublisher);

        final Map<String, List<Channel<T>>> channelsFinal = new HashMap<>(channels);

        dispatcher = new Fiber<>((SuspendableRunnable) () -> {

            for (; ; ) {
                T x = receiver.receive();
                if(x == null){
                    break;
                }

                List<Channel<T>> list = channelsFinal
                        .getOrDefault(extractorFunction.extactRoutingKeyFromObject(x).getKey(), new ArrayList<>());


                for(Channel<T> ch : list){
                    try {
                        ch.send(x);
                    } catch (SuspendExecution | InterruptedException suspendExecution) {
                        suspendExecution.printStackTrace();
                    }
                }
            }
        });
    }


    private Publisher<T> buildPublisher(QRoutingKey routingKey) {

        Channel<T> chan = Channels.newChannel(settings.getBufferSize(), settings.getOverflowPolicy());
        Publisher<T> pub = ReactiveStreams.toPublisher(chan);

        List<Channel<T>> list = channels.getOrDefault(routingKey.getKey(), new ArrayList<>());
        list.add(chan);
        channels.put(routingKey.getKey(), list);
        return pub;
    }

    @Override
    public Publisher<T> getPublisher() {
        return buildPublisher(QRoutingKey.broadcast());
    }

    @Override
    public Publisher<T> getPublisher(QRoutingKey routingKey) {

        if(extractorFunction == null){
            return getPublisher();
        }

        return buildPublisher(routingKey);
    }

    @Override
    public String toString() {
        return "EMITTER: "+((name == null) ? this.hashCode() : name);
    }

}
