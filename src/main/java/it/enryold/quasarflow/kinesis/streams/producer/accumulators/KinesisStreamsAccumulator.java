package it.enryold.quasarflow.kinesis.streams.producer.accumulators;

import it.enryold.quasarflow.abstracts.AbstractAccumulator;
import it.enryold.quasarflow.components.IAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsByteMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsRecordMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsStringMapper;
import it.enryold.quasarflow.kinesis.streams.producer.models.KinesisStreamsMapper;

import java.nio.ByteBuffer;
import java.util.List;

public class KinesisStreamsAccumulator<I> extends AbstractAccumulator<I, ByteBuffer> {

    protected KinesisStreamsMapper<I> kinesisStreamsMapper;

    public KinesisStreamsAccumulator(double byteSizeLimit, KinesisStreamsMapper<I> kinesisStreamsMapper, IAccumulatorLengthFunction<I> accumulatorLengthFunction)
    {
        super(byteSizeLimit);
        this.kinesisStreamsMapper = kinesisStreamsMapper;
        this.accumulatorLengthFunction = accumulatorLengthFunction;
    }

    @Override
    public List<ByteBuffer> getRecords() {

        IKinesisStreamsStringMapper<I> stringMapper = kinesisStreamsMapper.getStringMapper();
        IKinesisStreamsByteMapper byteMapper = kinesisStreamsMapper.getByteMapper();
        IKinesisStreamsRecordMapper<I> recordMapper = kinesisStreamsMapper.getRecordMapper();

        return recordMapper.apply(stringMapper, byteMapper)
                .apply(accumulator);
    }

    @Override
    public IAccumulatorLengthFunction<I> accumulatorLengthFunction() {
        return accumulatorLengthFunction;
    }
}
