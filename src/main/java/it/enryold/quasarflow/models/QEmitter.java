package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractEmitter;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.util.List;


public class QEmitter<T> extends AbstractEmitter<T> {



    public QEmitter(IFlow flow, String name) {
        super(flow, name);
    }

    public QEmitter(IFlow flow) {
        super(flow, null);
    }


    @Override
    public <EM extends IEmitter<T>> EM currentInstance() {
        return (EM)this;
    }

    @Override
    public <S extends IProcessor<T>> S addProcessor() {
        return  (S)new QProcessor<>(this);
    }

    @Override
    public <O> IFlatProcessor<O> addFlatProcessor() {
        IEmitter<List<O>> uncheckedCast = (IEmitter<List<O>>) this;
        return new QFlatProcessor<>(uncheckedCast);
    }


    @Override
    public <S extends IProcessor<T>> S addProcessor(QRoutingKey routingKey) {
        return  (S)new QProcessor<>( this, null, routingKey);
    }

    @Override
    public <S extends IProcessor<T>> S addProcessor(String name, QRoutingKey routingKey) {
        return  (S)new QProcessor<>( this, name, routingKey);
    }

    @Override
    public <S extends IConsumer<T>> S addConsumer() {
        return  (S)new QConsumer<>(this);
    }




}
