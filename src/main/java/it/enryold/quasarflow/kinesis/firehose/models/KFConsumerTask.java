package it.enryold.quasarflow.kinesis.firehose.models;

import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;
import it.enryold.quasarflow.interfaces.IConsumerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KFConsumerTask implements IConsumerTask<List<Record>> {

    private final Logger log = LoggerFactory.getLogger(getClass());


    private AmazonKinesisFirehose client;
    private String streamName;
    private boolean didLogRequests;


    public KFConsumerTask(AmazonKinesisFirehose client,
                          String streamName,
                          boolean didLogRequests){
        this.client = client;
        this.streamName = streamName;
        this.didLogRequests = didLogRequests;
    }


    @Override
    public void ingest(List<Record> records) {
        records.forEach(this::send);
    }

    private void send(Record record){
        try
        {

            PutRecordRequest putRecordRequest = new PutRecordRequest();
            putRecordRequest.setDeliveryStreamName(streamName);
            putRecordRequest.setRecord(record);
            client.putRecord(putRecordRequest);

            if(didLogRequests){
                log.debug("PutRecord in stream "+streamName+" with hash "+record.hashCode()+" and of "+record.getData().array().length+" bytes");
            }

        }
        catch (Exception e)
        {
            log.error("PutRecord in stream "+streamName+" failed "+e.getMessage());
            e.printStackTrace();
        }
    }

}
