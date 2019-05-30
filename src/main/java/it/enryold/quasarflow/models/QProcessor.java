package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.models.utils.QRoutingKey;

public class QProcessor<T> extends AbstractProcessor<T> {


    public QProcessor(IEmitter<T> eEmitter, String name, QRoutingKey routingKey) {
        super(eEmitter, name, routingKey);
    }

    public QProcessor(IEmitter<T> eEmitter, QRoutingKey routingKey) {
        super(eEmitter, routingKey);
    }

    public QProcessor(IEmitter<T> eEmitter) {
        super(eEmitter);
    }
}
