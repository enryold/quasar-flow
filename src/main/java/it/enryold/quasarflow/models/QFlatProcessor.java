package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractFlatProcessor;
import it.enryold.quasarflow.abstracts.AbstractProcessor;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IEmitterList;
import it.enryold.quasarflow.interfaces.ITransformFactory;
import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class QFlatProcessor<T> extends AbstractFlatProcessor<T> {


    public QFlatProcessor(IEmitter<List<T>> eEmitter, String name, QRoutingKey routingKey) {
        super(eEmitter, name, routingKey);
    }

    public QFlatProcessor(IEmitter<List<T>> eEmitter, QRoutingKey routingKey) {
        super(eEmitter, routingKey);
    }

    public QFlatProcessor(IEmitter<List<T>> eEmitter) {
        super(eEmitter);
    }
}
