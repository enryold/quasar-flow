package it.enryold.quasarflow.kinesis.firehose.accumulators.generic;

import it.enryold.quasarflow.components.IAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.firehose.models.FirehoseMapper;

public abstract class FirehoseAccumulatorLengthFunction<I> implements IAccumulatorLengthFunction<I>,
        IFirehoseAccumulatorLengthFunction<I> {

    protected FirehoseMapper<I> firehoseMapper;

    @Override
    public IFirehoseAccumulatorLengthFunction<I> withFirehoseMapper(FirehoseMapper<I> mapper) {
        this.firehoseMapper = mapper;
        return this;
    }


}
