package it.enryold.quasarflow.kinesis.streams.models;

import com.amazonaws.services.kinesis.model.Record;
import it.enryold.quasarflow.abstracts.AbstractEmitter;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QConsumer;
import it.enryold.quasarflow.models.QFlatProcessor;
import it.enryold.quasarflow.models.QProcessor;
import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.util.List;

public class KCLEmitter extends AbstractEmitter<Record> {


    public KCLEmitter(IFlow flow) {
        super(flow);
    }


    @Override
    public <EM extends IEmitter<Record>> EM currentInstance() {
        return (EM)this;
    }

    public <E extends IEmitter<Record>> E routedEmitter(IRoutingKeyExtractor<Record> extractor) {
        return super.routedEmitter(publisherChannel -> { }, extractor);
    }



    public <S extends IProcessor<Record>> S addProcessor(QRoutingKey partitionKey) {
        return (S)new QProcessor<>(this, null , partitionKey);
    }

    @Override
    public <S extends IProcessor<Record>> S addProcessor(String name, QRoutingKey partitionKey) {
        return (S)new QProcessor<>(this, name , partitionKey);
    }


    @Override
    public <S extends IConsumer<Record>> S addConsumer() {
        return  (S)new QConsumer<>(this);
    }




    @Override
    public <E extends IEmitter<Record>> E broadcastEmitter(IEmitterTask<Record> task) {
        throw new RuntimeException("Cannot instantiate a Kinesis Stream broadcast emitter");
    }

    @Override
    public <E extends IEmitter<Record>> E routedEmitter(IEmitterTask<Record> task, IRoutingKeyExtractor<Record> extractor) {
        throw new RuntimeException("Please use routedEmitter(IRoutingKeyExtractor extractor)");
    }

    @Override
    public <S extends IProcessor<Record>> S addProcessor() {
        throw new RuntimeException("Please use addProcessor(String partitionKey)");
    }

    @Override
    public <O> IFlatProcessor<O> addFlatProcessor() {
        throw new RuntimeException("Cannot add flatProcessor on this emitter");
    }


}
