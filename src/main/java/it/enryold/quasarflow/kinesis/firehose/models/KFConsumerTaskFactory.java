package it.enryold.quasarflow.kinesis.firehose.models;

import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.model.Record;
import it.enryold.quasarflow.interfaces.IConsumerTask;
import it.enryold.quasarflow.interfaces.IConsumerTaskFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KFConsumerTaskFactory implements IConsumerTaskFactory<List<Record>> {

    private AmazonKinesisFirehose client;
    private List<String> streamNames;
    private boolean didLogRequests;
    private AtomicInteger streamIterator = new AtomicInteger(0);


    private String getStreamName()
    {
        if(streamNames.size() == 1){
            return streamNames.get(0);
        }

        if(streamIterator.get() >= streamNames.size()){
            streamIterator.set(0);
        }
        return streamNames.get(streamIterator.getAndIncrement());
    }

    public KFConsumerTaskFactory(AmazonKinesisFirehose client,
                                 List<String> streamNames,
                                 boolean didLogRequests){
        this.client = client;
        this.streamNames = streamNames;
        this.didLogRequests = didLogRequests;
    }


    @Override
    public IConsumerTask<List<Record>> build() {
        return new KFConsumerTask(client, getStreamName(), didLogRequests);
    }
}
