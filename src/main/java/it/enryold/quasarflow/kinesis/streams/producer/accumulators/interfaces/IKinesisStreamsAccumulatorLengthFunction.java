package it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces;

import it.enryold.quasarflow.components.IAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.streams.producer.models.KinesisStreamsMapper;

public interface IKinesisStreamsAccumulatorLengthFunction<I> extends IAccumulatorLengthFunction<I> {

    IKinesisStreamsAccumulatorLengthFunction<I> withFirehoseMapper(KinesisStreamsMapper<I> mapper);
}
