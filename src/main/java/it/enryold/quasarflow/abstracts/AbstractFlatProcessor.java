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
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.models.QEmitterList;
import it.enryold.quasarflow.models.utils.FnBuildMetric;
import it.enryold.quasarflow.models.utils.QMetric;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import it.enryold.quasarflow.models.utils.QSettings;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public abstract class AbstractFlatProcessor<E> implements IFlatProcessor<E> {

    private final Logger log = LoggerFactory.getLogger(getClass());


    final protected List<Fiber<Void>> subscriberStrands = new ArrayList<>();
    protected Channel<QMetric> metricChannel;
    protected QSettings settings;


    private Fiber<Void> dispatcherStrand;
    private Channel<List<E>>[] rrChannels;
    final private List<ReceivePort<List<E>>> processorChannels = new ArrayList<>();
    private IEmitter<List<E>> emitter;
    private String name;
    private QRoutingKey routingKey;
    private IFlow flow;


    public AbstractFlatProcessor(IEmitter<List<E>> eEmitter, String name, QRoutingKey routingKey){
        this.flow = eEmitter.flow();
        this.emitter = eEmitter;
        this.settings = flow.getSettings();
        this.name = name == null ? getClass().getSimpleName()+this.hashCode() : name;
        this.routingKey = routingKey == null ? QRoutingKey.broadcast() : routingKey;
        flow.addStartable(this);
    }

    public AbstractFlatProcessor(IEmitter<List<E>> eEmitter, QRoutingKey routingKey){
        this(eEmitter, null, routingKey);
    }

    public AbstractFlatProcessor(IEmitter<List<E>> eEmitter){
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
        }else{
        }

    }

    @Override
    public IFlow flow() {
        return flow;
    }





    protected ReceivePort<E> buildProcessor(Publisher<List<E>> publisher)
    {
        final Processor<List<E>, E> processor = ReactiveStreams.toProcessor(settings.getBufferSize(), settings.getOverflowPolicy(), (SuspendableAction2<ReceivePort<List<E>>, SendPort<E>>) (in, out) -> {
            for (; ; ) {
                List<E> xs = in.receive();
                if (xs == null)
                    break;
                if(metricChannel != null) {
                    metricChannel.trySend(new FnBuildMetric().apply(this, QMetricType.RECEIVED.name()));
                }

                for(E x : xs){
                    out.send(x);
                }
            }
        });
        publisher.subscribe(processor);
        return ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), processor);
    }




    private List<Publisher<List<E>>> buildRRDispatcher(IEmitter<List<E>> emitter, int workers)
    {
        this.emitter = emitter;

        rrChannels = IntStream.range(0, workers)
                .mapToObj(i -> Channels.<List<E>>newChannel(settings.getBufferSize(), settings.getOverflowPolicy()))
                .toArray((IntFunction<Channel<List<E>>[]>) Channel[]::new);


        final ReceivePort<List<E>> roundRobinSubscriberChannel = ReactiveStreams.subscribe(settings.getBufferSize(), settings.getOverflowPolicy(), this.emitter.getPublisher(routingKey));
        dispatcherStrand = new Fiber<>((SuspendableRunnable) () -> {

            int index = 0;


            for (; ; ) {
                List<E> x = roundRobinSubscriberChannel.receive();
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
                    if(metricChannel != null) {
                        metricChannel.trySend(new FnBuildMetric().apply(this, QMetricType.PRODUCED.name()));
                    }
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






    public <EM extends IEmitter<E>> EM processWithFanIn(int workers){
        List<ReceivePort<E>> channels = buildRRDispatcher(emitter, workers)
                .stream()
                .map(this::buildProcessor)
                .peek(this::registerProcessorChannel)
                .collect(Collectors.toList());

       return (EM)buildFanInEmitter(channels);
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
