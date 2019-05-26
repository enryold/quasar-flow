package it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic;

import it.enryold.quasarflow.kinesis.streams.common.KinesisJsonListEncoder;
import it.enryold.quasarflow.kinesis.streams.common.interfaces.IKinesisListEncoder;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsStringMapper;

import java.util.List;

public class BasicKinesisStreamsJsonStringMapper<I> implements IKinesisStreamsStringMapper<I> {

    IKinesisListEncoder<I> encoder = new KinesisJsonListEncoder<>();

    @Override
    public String apply(List<I> s) {
        return encoder.apply(s);
    }
}
