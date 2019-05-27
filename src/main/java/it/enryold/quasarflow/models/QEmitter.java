package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractEmitter;
import it.enryold.quasarflow.interfaces.*;


public class QEmitter<T> extends AbstractEmitter<T> {



    public QEmitter(IFlow flow, String name) {
        super(flow, name);
    }

    public QEmitter(IFlow flow) {
        super(flow, null);
    }


    @Override
    public <S extends IProcessor<T>> S addProcessor() {
        return  (S)new QProcessor<>(flow, this);
    }

    @Override
    public <S extends IProcessor<T>> IEmitter<T> addProcessor(Injector<S> processor) {
        processor.accept((S)new QProcessor<>(flow, this));
        return this;
    }

    @Override
    public <S extends IProcessor<T>> S addProcessor(String routingKey) {
        return  (S)new QProcessor<>(flow, this, routingKey);
    }

    @Override
    public <S extends IProcessor<T>> IEmitter<T> addProcessor(String routingKey, Injector<S> processor) {
        processor.accept((S)new QProcessor<>(flow, this, routingKey));
        return this;
    }




    @Override
    public <S extends IConsumer<T>> S addConsumer() {
        return  (S)new QConsumer<>(flow, this);
    }

    @Override
    public <S extends IConsumer<T>> IEmitter<T> addConsumer(Injector<S> consumer) {
        consumer.accept(addConsumer());
        return this;
    }

}
