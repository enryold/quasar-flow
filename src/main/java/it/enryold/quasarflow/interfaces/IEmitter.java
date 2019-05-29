package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.strands.channels.Channel;
import org.reactivestreams.Publisher;

public interface IEmitter<T> extends IFlowable<T> {


    <EM extends IEmitter<T>> EM currentInstance();

    <E extends IEmitter<T>> E broadcastEmitter(IEmitterTask<T> task);
    <E extends IEmitter<T>> E routedEmitter(IEmitterTask<T> task, IRoutingKeyExtractor<T> extractorFactory);

    <S extends IProcessor<T>> S addProcessor();
    <S extends IProcessor<T>> S addProcessor(String routingKey);
    <S extends IConsumer<T>> S addConsumer();



    <S extends IProcessor<T>> IEmitter<T> addProcessor(Injector<S> processorInjector);
    <S extends IProcessor<T>> IEmitter<T> addProcessor(String routingKey, Injector<S> processorInjector);


    <S extends IProcessor<T>> S useProcessor(IEmitterInjector<T, S> emitterInjector);
    <S extends IProcessor<T>> IEmitter<T> useProcessor(IEmitterInjector<T, S> emitterInjector, Injector<S> processorInjector);

    <O> IOProcessor<T, O> ioProcessor(IEmitterInjector<T, IOProcessor<T, O>> emitterInjector);
    <O> IEmitter<O> ioProcessor(IEmitterInjector<T, IOProcessor<T, O>> emitterInjector, IOProcessorInjector<T, O, IEmitter<O>> processorInjector);



    <S extends IConsumer<T>> IEmitter<T> addConsumer(Injector<S> processorInjector);
    <S extends IConsumer<T>> S useConsumer(IEmitterInjector<T, S> emitterInjector);
    <S extends IConsumer<T>> IEmitter<T> useConsumer(IEmitterInjector<T, S> emitterInjector, Injector<S> processorInjector);


    Channel<T> getChannel();
    Publisher<T> getPublisher();
    Publisher<T> getPublisher(String routingKey);




}
