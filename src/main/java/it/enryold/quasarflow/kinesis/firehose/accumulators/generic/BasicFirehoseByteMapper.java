package it.enryold.quasarflow.kinesis.firehose.accumulators.generic;

import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseByteMapper;

import java.util.Optional;

public class BasicFirehoseByteMapper implements IFirehoseByteMapper {
    @Override
    public Optional<byte[]> apply(String s) {
        return Optional.of(s.getBytes());
    }
}
