package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractConsumer;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IFlow;

public class QConsumer<T> extends AbstractConsumer<T> {


    public QConsumer(IEmitter<T> eEmitter, String name) {
        super(eEmitter, name);
    }

    public QConsumer(IEmitter<T> eEmitter) {
        super(eEmitter);
    }
}
