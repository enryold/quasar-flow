package it.enryold.quasarflow.abstracts;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.*;
import co.paralleluniverse.strands.channels.reactivestreams.ReactiveStreams;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.IRoutingKeyExtractor;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEmitter<T> implements IEmitter<T> {

    private int emitterTaskChannelBuffer = 1_000_000;
    private Channels.OverflowPolicy emitterTaskChannelOverflowPolicy = Channels.OverflowPolicy.BLOCK;
    private int broadcasterChannelBuffer = 1_000_000;
    private Channels.OverflowPolicy broadcasterChannelOverflowPolicy = Channels.OverflowPolicy.BLOCK;
    protected Channel<T> emitterTaskChannel;
    private Fiber emitterTaskStrand;
    private Publisher<T> emitterTaskPublisher;
    private Map<String, List<Channel<T>>> channels = new HashMap<>();
    private IEmitterTask<T> task;
    private Fiber dispatcher;
    private String name;
    private IRoutingKeyExtractor<T> extractorFunction;
    protected IFlow flow;


    public AbstractEmitter(IFlow flow){
        this(flow, null);
    }

    public AbstractEmitter(IFlow flow, String name){
        this.flow = flow;
        this.name = name == null ? String.valueOf(this.hashCode()) : name;
        flow.addStartable(this);
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
        emitterTaskChannel = Channels.newChannel(emitterTaskChannelBuffer, emitterTaskChannelOverflowPolicy);
        emitterTaskStrand = new Fiber<Void>((SuspendableRunnable) () -> task.emit(emitterTaskChannel));
        emitterTaskPublisher = ReactiveStreams.toPublisher(emitterTaskChannel);
        return (E)this;
    }

    @Override
    public <E extends IEmitter<T>> E routedEmitter(IEmitterTask<T> task, IRoutingKeyExtractor<T> extractor) {
        this.task = task;
        this.extractorFunction = extractor;
        emitterTaskChannel = Channels.newChannel(emitterTaskChannelBuffer, emitterTaskChannelOverflowPolicy);
        emitterTaskStrand = new Fiber<Void>((SuspendableRunnable) () -> task.emit(emitterTaskChannel));
        emitterTaskPublisher = ReactiveStreams.toPublisher(emitterTaskChannel);
        return (E)this;
    }


    @Override
    public void start() {

        if(extractorFunction == null){
            buildBroadcaster();
        }else{
            buildRouted();
        }

        System.out.println("Start EMITTER "+name+" publisher strand "+emitterTaskStrand.getName());
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
        channels.entrySet().stream().flatMap(s -> s.getValue().stream()).forEach(SendPort::close);
    }

    private void buildBroadcaster()
    {
        ReceivePort<T> receiver = ReactiveStreams.subscribe(broadcasterChannelBuffer, broadcasterChannelOverflowPolicy, emitterTaskPublisher);
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
        ReceivePort<T> receiver = ReactiveStreams.subscribe(broadcasterChannelBuffer, broadcasterChannelOverflowPolicy, emitterTaskPublisher);

        final Map<String, List<Channel<T>>> channelsFinal = new HashMap<>(channels);

        dispatcher = new Fiber<>((SuspendableRunnable) () -> {

            for (; ; ) {
                T x = receiver.receive();
                if(x == null){
                    break;
                }

                List<Channel<T>> list = extractorFunction
                        .extactRoutingKeyFromObject(x)
                        .map(channelsFinal::get)
                        .orElse(new ArrayList<>());


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


    private Publisher<T> buildPublisher(String routingKey) {

        Channel<T> chan = Channels.newChannel(emitterTaskChannelBuffer, emitterTaskChannelOverflowPolicy);
        Publisher<T> pub = ReactiveStreams.toPublisher(chan);

        List<Channel<T>> list = channels.getOrDefault(routingKey, new ArrayList<>());
        list.add(chan);
        channels.put(routingKey, list);
        return pub;
    }

    @Override
    public Publisher<T> getPublisher() {
        return buildPublisher("BROADCAST");
    }

    @Override
    public Publisher<T> getPublisher(String routingKey) {

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
