package it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces;

import co.paralleluniverse.fibers.Suspendable;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface IKinesisStreamsByteMapper {

    @Suspendable
    Optional<byte[]> apply(String i);
}
