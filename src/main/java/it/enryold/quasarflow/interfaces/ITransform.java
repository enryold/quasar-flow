package it.enryold.quasarflow.interfaces;

import java.util.function.Function;

@FunctionalInterface
public interface ITransform<I, T> extends Function<I, T> {
}
