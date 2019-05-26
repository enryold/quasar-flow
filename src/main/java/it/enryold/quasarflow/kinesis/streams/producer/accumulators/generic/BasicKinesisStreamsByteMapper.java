package it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic;

import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsByteMapper;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class BasicKinesisStreamsByteMapper implements IKinesisStreamsByteMapper {
    @Override
    public Optional<byte[]> apply(String s) {
        try {
            return Optional.of(s.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
