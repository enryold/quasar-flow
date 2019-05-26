package it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces;

import it.enryold.quasarflow.components.IAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.firehose.models.FirehoseMapper;

public interface IFirehoseAccumulatorLengthFunction<I> extends IAccumulatorLengthFunction<I> {

    IFirehoseAccumulatorLengthFunction<I> withFirehoseMapper(FirehoseMapper<I> mapper);
}
