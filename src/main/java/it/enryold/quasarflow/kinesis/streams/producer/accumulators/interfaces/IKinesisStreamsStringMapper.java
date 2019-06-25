package it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces;

import co.paralleluniverse.fibers.Suspendable;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface IKinesisStreamsStringMapper<In> {

    @Suspendable
    String apply(List<In> i);
}
