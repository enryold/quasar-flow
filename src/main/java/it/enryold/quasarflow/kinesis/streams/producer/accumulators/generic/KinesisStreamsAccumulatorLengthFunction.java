package it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic;

import it.enryold.quasarflow.components.IAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.streams.producer.models.KinesisStreamsMapper;

public abstract class KinesisStreamsAccumulatorLengthFunction<I> implements IAccumulatorLengthFunction<I>, IKinesisStreamsAccumulatorLengthFunction<I> {

    protected KinesisStreamsMapper<I> kinesisStreamsMapper;

    @Override
    public IKinesisStreamsAccumulatorLengthFunction<I> withFirehoseMapper(KinesisStreamsMapper<I> mapper) {
        this.kinesisStreamsMapper = mapper;
        return this;
    }


}
