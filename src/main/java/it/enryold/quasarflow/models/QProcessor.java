package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IFlow;

public class QProcessor<T> extends AbstractProcessor<T> {


    public QProcessor(IFlow flow, IEmitter<T> eEmitter, String name, String routingKey) {
        super(flow, eEmitter, name, routingKey);
    }

    public QProcessor(IFlow flow, IEmitter<T> eEmitter, String routingKey) {
        super(flow, eEmitter, routingKey);
    }

    public QProcessor(IFlow flow, IEmitter<T> eEmitter) {
        super(flow, eEmitter);
    }
}
