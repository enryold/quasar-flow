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
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.models.QEmitterList;
import it.enryold.quasarflow.models.utils.QEmitterChannel;
import it.enryold.quasarflow.models.utils.QRoutingKey;
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


public abstract class AbstractIOProcessor<E, O> extends AbstractFlowable implements IOProcessor<E, O> {

    private final Logger log = LoggerFactory.getLogger(getClass());


    final protected List<Fiber<Void>> subscriberStrands = new ArrayList<>();


    private Fiber<Void> dispatcherStrand;
    private Channel<E>[] rrChannels;
    final private List<ReceivePort<E>> processorChannels = new ArrayList<>();
    private IEmitter<E> emitter;
    private QRoutingKey routingKey;
    protected IOProcessorAsyncTaskBuilder<E, O> processorAsyncTaskBuilder;


    public AbstractIOProcessor(IEmitter<E> eEmitter, String name, QRoutingKey routingKey){
        this.flow = eEmitter.flow();
        this.emitter = eEmitter;
        this.settings = flow.getSettings();
        String routingKeyString = (routingKey != null) ? " ("+routingKey.getKey()+")" : "";
        super.setName(name == null ? getClass().getSimpleName()+this.hashCode()+routingKeyString : name+routingKeyString);
        this.routingKey = routingKey == null ? QRoutingKey.broadcast() : routingKey;
        flow.addStartable(this);
    }

    public AbstractIOProcessor(IEmitter<E> eEmitter, QRoutingKey routingKey){
        this(eEmitter, null, routingKey);
    }

    public AbstractIOProcessor(IEmitter<E> eEmitter){
        this(eEmitter, null);
    }


    @Override
    public IFlowable parent() {
        return emitter;
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
    public IOProcessor<E, O> withAsyncTaskBuilder(IOProcessorAsyncTaskBuilder<E, O> processorAsyncTaskBuilder) {
        this.processorAsyncTaskBuilder = processorAsyncTaskBuilder;
        return this;
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




    private ReceivePort<O> buildProcessor(Publisher<E> publisher)
    {


        final Processor<E, O> processor = ReactiveStreams.toProcessor(10, Channels.OverflowPolicy.BLOCK, (SuspendableAction2<ReceivePort<E>, SendPort<O>>) (in, out) -> {
            for (; ; ) {
                E x = in.receive();
                if (x == null)
                    continue;
                receivedElements.incrementAndGet();

                processorAsyncTaskBuilder.build().async(x, out);
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
        final QEmitterChannel<I> qEmitterChannel = new QEmitterChannel<>(publisherChannel);
        final IEmitterTask<I> task = this.buildEmitterTask(channel);
        return new Fiber<>((SuspendableRunnable) () -> task.emitOn(qEmitterChannel));
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

    private  <I> IEmitterTask<I> buildEmitterTask(ReceivePort<I> channel)
    {
        return publisherChannel -> {
            for (; ; ) {
                try {
                    I x = channel.receive();
                    if (x == null)
                    continue;

                    publisherChannel.sendOnChannel(x);
                } catch (InterruptedException e) {
                    log("buildEmitterTask Strand interrupted: " + Strand.currentStrand().getName());
                } catch (Exception e) {
                    error("buildEmitterTask Strand in Exception: " + Strand.currentStrand().getName() + " - Message: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        };
    }



    public <EM extends IEmitter<O>> EM process(){
        ReceivePort<O> processor = this.buildProcessor(emitter.getPublisher(routingKey));
        this.registerProcessorChannel(processor);
        return new QEmitter<O>(flow).broadcastEmitter(buildEmitterTask(processor));
    }




    public IEmitterList<O> processWithFanOut(int workers){
        List<IEmitter<O>> emitters = buildRRDispatcher(emitter, workers)
                .stream()
                .map(this::buildProcessor)
                .peek(this::registerProcessorChannel)
                .map(this::buildEmitterTask)
                .map(task -> new QEmitter<O>(flow).broadcastEmitter(task))
                .map(e -> (IEmitter<O>)e)
                .collect(Collectors.toList());

        return new QEmitterList<>(emitters);

    }



    public <EM extends IEmitter<O>> EM processWithFanIn(int workers){
        List<ReceivePort<O>> channels = buildRRDispatcher(emitter, workers)
                .stream()
                .map(this::buildProcessor)
                .peek(this::registerProcessorChannel)
                .collect(Collectors.toList());

       return (EM)buildFanInEmitter(channels);
    }





    @Override
    public String toString() {
        return "IOProcessor: "+this.getName();
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
