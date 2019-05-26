package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractConsumer;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IFlow;

public class QConsumer<T> extends AbstractConsumer<T> {


    public QConsumer(IFlow flow, IEmitter<T> tEmitter, String name) {
        super(flow, tEmitter, name);
    }

    public QConsumer(IFlow flow, IEmitter<T> tEmitter) {
        super(flow, tEmitter);
    }
}
