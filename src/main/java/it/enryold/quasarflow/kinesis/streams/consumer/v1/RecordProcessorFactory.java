package it.enryold.quasarflow.kinesis.streams.consumer.v1;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.model.Record;

/**
 * Created by enryold on 20/12/16.
 */

public class RecordProcessorFactory implements IRecordProcessorFactory {


    private int backoffTime;
    private int retriesNumber;
    private int checkpointInterval;
    private Channel<Record> emitterChannel;




    public RecordProcessorFactory withBackoffTime(int backoffTime)
    {
        this.backoffTime = backoffTime;
        return this;
    }

    public RecordProcessorFactory withRetriesNumber(int retriesNumber)
    {
        this.retriesNumber = retriesNumber;
        return this;
    }

    public RecordProcessorFactory withCheckpointInterval(int checkpointInterval)
    {
        this.checkpointInterval = checkpointInterval;
        return this;
    }

    public RecordProcessorFactory withEmitterChannel(Channel<Record> emitterChannel)
    {
        this.emitterChannel = emitterChannel;
        return this;
    }


    @Override
    @Suspendable
    public IRecordProcessor createProcessor()
    {
        return new RecordProcessor()
                .withBackoffTime(backoffTime)
                .withCheckpointInterval(checkpointInterval)
                .withRetriesNumber(retriesNumber)
                .withEmitterChannel(emitterChannel);

    }
}
