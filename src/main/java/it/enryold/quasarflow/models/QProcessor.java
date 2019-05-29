package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IFlow;

public class QProcessor<T> extends AbstractProcessor<T> {


    public QProcessor(IEmitter<T> eEmitter, String name, String routingKey) {
        super(eEmitter, name, routingKey);
    }

    public QProcessor(IEmitter<T> eEmitter, String routingKey) {
        super(eEmitter, routingKey);
    }

    public QProcessor(IEmitter<T> eEmitter) {
        super(eEmitter);
    }
}
