package it.enryold.quasarflow.components;

import java.util.function.Function;

@FunctionalInterface
public interface IAccumulatorLengthFunction<I> extends Function<I, Double> {
}
