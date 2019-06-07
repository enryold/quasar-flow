package it.enryold.quasarflow.kinesis.streams;

import co.paralleluniverse.strands.channels.Channel;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import it.enryold.quasarflow.kinesis.streams.consumer.v1.RecordProcessorFactory;
import it.enryold.quasarflow.kinesis.streams.models.KCLEmitter;
import it.enryold.quasarflow.kinesis.streams.models.KCLFlow;
import it.enryold.quasarflow.models.metrics.QMetric;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import it.enryold.quasarflow.models.utils.QSettings;

public class KSQFlow {


    private KCLFlow flow;

    private KSQFlow(KinesisClientLibConfiguration kinesisClientLibConfiguration,
                    QSettings settings){
        flow = new KCLFlow(kinesisClientLibConfiguration, settings);
    }

    private KSQFlow(KinesisClientLibConfiguration kinesisClientLibConfiguration){
        flow = new KCLFlow(kinesisClientLibConfiguration);
    }

    public static KSQFlow newFlow(KinesisClientLibConfiguration kinesisClientLibConfiguration){
        return new KSQFlow(kinesisClientLibConfiguration);
    }

    public KCLEmitter kinesisEmitter(RecordProcessorFactory recordProcessorFactory){

        KCLEmitter emitter = new KCLEmitter(flow)
                .routedEmitter(o -> QRoutingKey.withKey(o.getPartitionKey()));

        flow.setRecordProcessorFactory(recordProcessorFactory.withEmitterChannel(emitter.getChannel()));
        return emitter;
    }


}
