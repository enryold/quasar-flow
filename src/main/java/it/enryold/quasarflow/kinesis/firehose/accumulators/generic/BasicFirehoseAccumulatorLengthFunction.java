package it.enryold.quasarflow.kinesis.firehose.accumulators.generic;

import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseByteMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseStringMapper;

import java.util.Collections;

public class BasicFirehoseAccumulatorLengthFunction<I> extends FirehoseAccumulatorLengthFunction<I> {

    @Override
    public Double apply(I i) {
        IFirehoseStringMapper<I> stringMapper = firehoseMapper.getStringMapper();
        IFirehoseByteMapper byteMapper = firehoseMapper.getByteMapper();

        return Double.valueOf(stringMapper
                .andThen(str -> byteMapper.apply(str).map(b -> b.length).orElse(0))
                .apply(Collections.singletonList(i)));
    }
}
