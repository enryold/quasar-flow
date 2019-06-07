package it.enryold.quasarflow.abstracts;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.ReceivePort;
import co.paralleluniverse.strands.channels.SendPort;
import co.paralleluniverse.strands.channels.reactivestreams.ReactiveStreams;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.utils.QEmitterChannel;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractEmitter<T> extends AbstractFlowable implements IEmitter<T> {

    protected Channel<T> emitterTaskChannel;
    private Fiber emitterTaskStrand;
    private Publisher<T> emitterTaskPublisher;
    final private Map<String, List<Channel<T>>> channels = new HashMap<>();
    private IEmitterTask<T> task;
    private Fiber dispatcher;
    private IRoutingKeyExtractor<T> extractorFunction;


    public AbstractEmitter(IFlow flow){
        this(flow, null);
    }

    public AbstractEmitter(IFlow flow, String name){
        this.flow = flow;
        this.settings = flow.getSettings();
        super.setName(name == null ? getClass().getSimpleName()+this.hashCode() : name);
        flow.addStartable(this);
    }

    @Override
    public IFlowable parent() {
        return null;
    }

    public <E extends IEmitter<T>> E broadcast(){
        return this.broadcastEmitter(task);
    }

    public <E extends IEmitter<T>> E routed(IRoutingKeyExtractor<T> extractor){
        return this.routedEmitter(task, extractor);
    }

    private QEmitterChannel<T> getInputChannel() {
        return new QEmitterChannel<>(emitterTaskChannel, t -> producedElements.incrementAndGet());
    }

    public <E extends IEmitter<T>> E broadcastEmitter(IEmitterTask<T> task)
    {
        this.task = task;
        emitterTaskChannel = Channels.newChannel(settings.getBufferSize(), settings.getOverflowPolicy());

        final QEmitterChannel<T> qEmitterChannel = getInputChannel();

        emitterTaskStrand = new Fiber<Void>((SuspendableRunnable) () -> { if(task != null){
            task.emitOn(qEmitterChannel);
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

        final QEmitterChannel<T> qEmitterChannel = getInputChannel();

        emitterTaskStrand = new Fiber<Void>((SuspendableRunnable) () -> { if(task != null){
            task.emitOn(qEmitterChannel);
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
        flow.setNested(processor);
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public IEmitter<T> addProcessor(String name, Injector<IProcessor<T>> processorInjector){
        IEmitter<T> emitter = currentInstance();
        IProcessor<T> processor = addProcessor(name);
        flow.setNested(processor);
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public IEmitter<T> addProcessor(QRoutingKey routingKey, Injector<IProcessor<T>> processorInjector){
        IEmitter<T> emitter = currentInstance();
        IProcessor<T> processor = addProcessor(routingKey);
        flow.setNested(processor);
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public IEmitter<T> addProcessor(String name, QRoutingKey routingKey, Injector<IProcessor<T>> processorInjector){
        IEmitter<T> emitter = currentInstance();
        IProcessor<T> processor = addProcessor(name, routingKey);
        flow.setNested(processor);
        processorInjector.accept(processor);
        return emitter;
    }

    @Override
    public <O> IFlatProcessor<O> addFlatProcessor(String name) {
        IFlatProcessor<O> flatProcessor = this.addFlatProcessor();
        flatProcessor.setName(name);
        return flatProcessor;
    }

    @Override
    public IEmitter<T> addConsumer(Injector<IConsumer<T>> consumerInjector) {
        IEmitter<T> emitter = currentInstance();
        IConsumer<T> consumer = addConsumer();
        flow.setNested(consumer);
        consumerInjector.accept(consumer);
        return emitter;
    }

    @Override
    public IEmitter<T> addConsumer(String name, Injector<IConsumer<T>> consumerInjector) {
        IEmitter<T> emitter = currentInstance();
        IConsumer<T> consumer = addConsumer(name);
        flow.setNested(consumer);
        consumerInjector.accept(consumer);
        return emitter;
    }

    @Override
    public <S> IEmitter<S> map(InjectorEmitter<T, S> injector){
        IEmitter<S> emitter = injector.injectEmitter(currentInstance());
        flow.setNested(emitter);
        return emitter;
    }


    @Override
    public IEmitter<T> consume(InjectorConsumer<T> injector) {
        IEmitter<T> emitter = currentInstance();
        injector.injectConsumer(emitter);
        flow.setParentNested();
        return emitter;
    }

    @Override
    public <S extends IConsumer<T>> S addConsumer(String name) {
        S consumer = addConsumer();
        consumer.setName(name);
        flow.setParentNested();
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
        log("START Dispatcher Strand "+dispatcher.getName());

        emitterTaskStrand.start();
        log("START Emitter task Strand "+emitterTaskStrand.getName());

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
        return "Emitter: "+this.getName();
    }

}
