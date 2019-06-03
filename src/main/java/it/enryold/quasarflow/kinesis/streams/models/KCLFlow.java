package it.enryold.quasarflow.kinesis.streams.models;

import co.paralleluniverse.strands.channels.Channel;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import it.enryold.quasarflow.abstracts.AbstractFlow;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.kinesis.streams.consumer.v1.RecordProcessorFactory;
import it.enryold.quasarflow.models.metrics.QMetric;
import it.enryold.quasarflow.models.utils.QSettings;


public class KCLFlow extends AbstractFlow {

    private Worker kclWorker;
    private IRecordProcessorFactory recordProcessorFactory;
    private KinesisClientLibConfiguration kinesisClientLibConfiguration;


    public KCLFlow(
                   KinesisClientLibConfiguration kinesisClientLibConfiguration,
                   QSettings settings,  Channel<QMetric> metricChannel){
        super(settings, metricChannel);
        this.kinesisClientLibConfiguration = kinesisClientLibConfiguration;

    }

    public KCLFlow(
            KinesisClientLibConfiguration kinesisClientLibConfiguration){
        super();
        this.kinesisClientLibConfiguration = kinesisClientLibConfiguration;
    }




    public void setRecordProcessorFactory(RecordProcessorFactory recordProcessorFactory) {
        this.recordProcessorFactory = recordProcessorFactory;
    }


    @Override
    public <I extends IFlow> I start() {
        super.start();
        kclWorker = new Worker.Builder()
                .recordProcessorFactory(recordProcessorFactory)
                .config(kinesisClientLibConfiguration)
                .build();
        kclWorker.run();
        return (I)this;
    }

}
