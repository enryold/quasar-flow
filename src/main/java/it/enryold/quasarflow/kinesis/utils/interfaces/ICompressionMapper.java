package it.enryold.quasarflow.kinesis.utils.interfaces;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface ICompressionMapper extends Function<byte[], Optional<byte[]>> {
}
