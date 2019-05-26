package it.enryold.quasarflow.kinesis.firehose.models;

import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseByteMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseRecordMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseStringMapper;

public class FirehoseMapper<I> {

    private IFirehoseStringMapper<I> stringMapper;
    private IFirehoseRecordMapper<I> recordMapper;
    private IFirehoseByteMapper byteMapper;

    private FirehoseMapper(Builder<I> builder) {
        stringMapper = builder.stringMapper;
        recordMapper = builder.recordMapper;
        byteMapper = builder.byteMapper;
    }

    public static <I>Builder<I> newBuilder() {
        return new Builder<>();
    }

    public IFirehoseStringMapper<I> getStringMapper() {
        return stringMapper;
    }

    public IFirehoseRecordMapper<I> getRecordMapper() {
        return recordMapper;
    }

    public IFirehoseByteMapper getByteMapper() {
        return byteMapper;
    }

    public static final class Builder<I> {
        private IFirehoseStringMapper<I> stringMapper;
        private IFirehoseRecordMapper<I> recordMapper;
        private IFirehoseByteMapper byteMapper;

        private Builder() {
        }

        public Builder<I> withStringMapper(IFirehoseStringMapper<I> val) {
            stringMapper = val;
            return this;
        }

        public Builder<I> withRecordMapper(IFirehoseRecordMapper<I> val) {
            recordMapper = val;
            return this;
        }

        public Builder<I> withByteMapper(IFirehoseByteMapper val) {
            byteMapper = val;
            return this;
        }

        public FirehoseMapper<I> build() {
            return new FirehoseMapper<>(this);
        }
    }
}
