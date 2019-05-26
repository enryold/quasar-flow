package it.enryold.quasarflow.kinesis.streams.producer.models;

import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsByteMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsRecordMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsStringMapper;

public class KinesisStreamsMapper<I> {

    private IKinesisStreamsStringMapper<I> stringMapper;
    private IKinesisStreamsRecordMapper<I> recordMapper;
    private IKinesisStreamsByteMapper byteMapper;

    private KinesisStreamsMapper(Builder builder) {
        stringMapper = builder.stringMapper;
        recordMapper = builder.recordMapper;
        byteMapper = builder.byteMapper;
    }

    public static Builder newBuilder() {
        return new Builder<>();
    }

    public IKinesisStreamsStringMapper<I> getStringMapper() {
        return stringMapper;
    }

    public IKinesisStreamsRecordMapper<I> getRecordMapper() {
        return recordMapper;
    }

    public IKinesisStreamsByteMapper getByteMapper() {
        return byteMapper;
    }

    public static final class Builder<I> {
        private IKinesisStreamsStringMapper<I> stringMapper;
        private IKinesisStreamsRecordMapper<I> recordMapper;
        private IKinesisStreamsByteMapper byteMapper;

        private Builder() {
        }

        public Builder withStringMapper(IKinesisStreamsStringMapper<I> val) {
            stringMapper = val;
            return this;
        }

        public Builder withRecordMapper(IKinesisStreamsRecordMapper<I> val) {
            recordMapper = val;
            return this;
        }

        public Builder withByteMapper(IKinesisStreamsByteMapper val) {
            byteMapper = val;
            return this;
        }

        public KinesisStreamsMapper build() {
            return new KinesisStreamsMapper(this);
        }
    }
}
