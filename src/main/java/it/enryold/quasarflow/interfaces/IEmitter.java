package it.enryold.quasarflow.interfaces;

import org.reactivestreams.Publisher;

public interface IEmitter<T> extends IFlowable {

    <E extends IEmitter<T>> E broadcastEmitter(IEmitterTask<T> task);
    <E extends IEmitter<T>> E routedEmitter(IEmitterTask<T> task, IRoutingKeyExtractor<T> extractorFactory);

    <S extends IProcessor<T>> S addProcessor();
    <S extends IProcessor<T>> IEmitter<T> addProcessor(Injector<S> processor);

    <S extends IProcessor<T>> S addProcessor(String routingKey);
    <S extends IProcessor<T>> IEmitter<T> addProcessor(String routingKey, Injector<S> processor);

    <S extends IConsumer<T>> S addConsumer();
    <S extends IConsumer<T>> IEmitter<T> addConsumer(Injector<S> consumer);


    Publisher<T> getPublisher();
    Publisher<T> getPublisher(String routingKey);




}
