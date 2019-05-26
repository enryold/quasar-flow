package it.enryold.quasarflow.kinesis.streams.models;


import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import it.enryold.quasarflow.interfaces.IConsumerTask;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitHashProviderFactory;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

public class KPLConsumerTask implements IConsumerTask<List<ByteBuffer>> {


    private IExplicitHashProviderFactory hashProviderFactory;
    private KinesisProducer kinesisProducer;
    private Consumer<ListenableFuture<UserRecordResult>> consumerCallback;
    private String streamName;
    private String partitionKey;

    private KPLConsumerTask(Builder builder) {
        hashProviderFactory = builder.hashProviderFactory;
        kinesisProducer = builder.kinesisProducer;
        consumerCallback = builder.consumerCallback;
        streamName = builder.streamName;
        partitionKey = builder.partitionKey;
    }

    public static Builder Builder() {
        return new Builder();
    }


    @Override
    public void ingest(List<ByteBuffer> elms) {
        elms.forEach(this::send);
    }

    private void send( ByteBuffer elm){
        ListenableFuture<UserRecordResult> future = kinesisProducer.addUserRecord(streamName,
                partitionKey,
                hashProviderFactory.getHashProvider().nextHashKey(),
                elm);

        if(consumerCallback != null){
            consumerCallback.accept(future);
        }
    }

    public static final class Builder {
        private IExplicitHashProviderFactory hashProviderFactory;
        private KinesisProducer kinesisProducer;
        private Consumer<ListenableFuture<UserRecordResult>> consumerCallback;
        private String streamName;
        private String partitionKey;

        private Builder() {
        }

        public Builder withHashProviderFactory(IExplicitHashProviderFactory val) {
            hashProviderFactory = val;
            return this;
        }

        public Builder withKinesisProducer(KinesisProducer val) {
            kinesisProducer = val;
            return this;
        }

        public Builder withConsumerCallback(Consumer<ListenableFuture<UserRecordResult>> val) {
            consumerCallback = val;
            return this;
        }

        public Builder withStreamName(String val) {
            streamName = val;
            return this;
        }

        public Builder withPartitionKey(String val) {
            partitionKey = val;
            return this;
        }


        public KPLConsumerTask build() {
            return new KPLConsumerTask(this);
        }
    }
}
