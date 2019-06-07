package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import org.reactivestreams.Publisher;

public interface IEmitter<T> extends IFlowable {




    // Methods for concrete classes.
    <EM extends IEmitter<T>> EM currentInstance();
    <S extends IProcessor<T>> S addProcessor();
    <O> IFlatProcessor<O> addFlatProcessor();
    <S extends IConsumer<T>> S addConsumer();



    // Abstracts

    // Flow
    IEmitter<T> consume(InjectorConsumer<T> injector);

    // Emitter
    <S> IEmitter<S> map(InjectorEmitter<T, S> injector);

    <E extends IEmitter<T>> E broadcastEmitter(IEmitterTask<T> task);
    <E extends IEmitter<T>> E routedEmitter(IEmitterTask<T> task, IRoutingKeyExtractor<T> extractorFactory);
    <E extends IEmitter<T>> E broadcast();
    <E extends IEmitter<T>> E routed(IRoutingKeyExtractor<T> extractor);


    // Processor
    <S extends IProcessor<T>> S addProcessor(String name);
    <S extends IProcessor<T>> S addProcessor(QRoutingKey routingKey);
    <S extends IProcessor<T>> S addProcessor(String name, QRoutingKey routingKey);

    IEmitter<T> addProcessor(Injector<IProcessor<T>> processorInjector);
    IEmitter<T> addProcessor(String name, Injector<IProcessor<T>> processorInjector);
    IEmitter<T> addProcessor(QRoutingKey routingKey, Injector<IProcessor<T>> processorInjector);
    IEmitter<T> addProcessor(String name, QRoutingKey routingKey, Injector<IProcessor<T>> processorInjector);

    <O> IFlatProcessor<O> addFlatProcessor(String name);


    // Consumer
    <S extends IConsumer<T>> S addConsumer(String name);

    IEmitter<T> addConsumer(Injector<IConsumer<T>> consumerInjector);
    IEmitter<T> addConsumer(String name, Injector<IConsumer<T>> consumerInjector);



    // Utils
    Channel<T> getChannel();
    Publisher<T> getPublisher();
    Publisher<T> getPublisher(QRoutingKey routingKey);




}
