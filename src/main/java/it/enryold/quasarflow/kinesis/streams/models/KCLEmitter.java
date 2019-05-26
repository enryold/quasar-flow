package it.enryold.quasarflow.kinesis.streams.models;

import co.paralleluniverse.strands.channels.Channel;
import com.amazonaws.services.kinesis.model.Record;
import it.enryold.quasarflow.models.QConsumer;
import it.enryold.quasarflow.models.QProcessor;
import it.enryold.quasarflow.abstracts.AbstractEmitter;
import com.fulmicotone.cio.interfaces.*;
import it.enryold.quasarflow.interfaces.*;

public class KCLEmitter extends AbstractEmitter<Record> {


    public KCLEmitter(IFlow flow) {
        super(flow);
    }




    public <E extends IEmitter<Record>> E routedEmitter(IRoutingKeyExtractor<Record> extractor) {
        return super.routedEmitter(publisherChannel -> { }, extractor);
    }


    public Channel<Record> getChannel(){
        return emitterTaskChannel;
    }

    public <S extends IProcessor<Record>> S addProcessor(String partitionKey) {
        return (S)new QProcessor<>(flow, this, partitionKey);
    }

    public <S extends IProcessor<Record>> KCLEmitter addProcessor(String partitionKey, Injector<S> consumer) {
        consumer.accept(addProcessor(partitionKey));
        return this;
    }

    @Override
    public <S extends IConsumer<Record>> S addConsumer() {
        return  (S)new QConsumer<>(flow, this);
    }

    @Override
    public <S extends IConsumer<Record>> KCLEmitter addConsumer(Injector<S> consumer) {
        consumer.accept(addConsumer());
        return this;
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
    public <S extends IProcessor<Record>> IEmitter<Record> addProcessor(Injector<S> consumer) {
        throw new RuntimeException("Please use addProcessor(QConsumer<S> consumer, String partitionKey)");
    }
}
