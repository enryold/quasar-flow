package it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface IKinesisStreamsByteMapper extends Function<String, Optional<byte[]>> {
}
