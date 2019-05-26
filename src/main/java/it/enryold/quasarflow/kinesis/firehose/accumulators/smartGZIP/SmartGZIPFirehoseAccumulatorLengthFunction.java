package it.enryold.quasarflow.kinesis.firehose.accumulators.smartGZIP;

import it.enryold.quasarflow.kinesis.firehose.accumulators.generic.FirehoseAccumulatorLengthFunction;

import java.util.Collections;

public class SmartGZIPFirehoseAccumulatorLengthFunction<I> extends FirehoseAccumulatorLengthFunction<I> {

    @Override
    public Double apply(I i) {
        double size = firehoseMapper.getStringMapper().apply(Collections.singletonList(i)).length();
        return Math.ceil(size / SmartGZIPFirehoseRecordMapper.COMPRESSION_RATIO);
    }
}
