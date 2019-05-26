package it.enryold.quasarflow.kinesis.streams;

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import it.enryold.quasarflow.kinesis.streams.consumer.v1.RecordProcessorFactory;
import it.enryold.quasarflow.kinesis.streams.models.KCLEmitter;
import it.enryold.quasarflow.kinesis.streams.models.KCLFlow;

import java.util.Optional;

public class KSQFlow {


    private KCLFlow flow;

    private KSQFlow(KinesisClientLibConfiguration kinesisClientLibConfiguration){

        flow = new KCLFlow(kinesisClientLibConfiguration);

    }

    public static KSQFlow newFlow(KinesisClientLibConfiguration kinesisClientLibConfiguration){
        return new KSQFlow(kinesisClientLibConfiguration);
    }

    public KCLEmitter kinesisEmitter(RecordProcessorFactory recordProcessorFactory){

        KCLEmitter emitter = new KCLEmitter(flow)
                .routedEmitter(o -> Optional.ofNullable(o.getPartitionKey()));

        flow.setRecordProcessorFactory(recordProcessorFactory.withEmitterChannel(emitter.getChannel()));
        return emitter;
    }


}
