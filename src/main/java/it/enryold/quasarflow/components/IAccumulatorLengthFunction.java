package it.enryold.quasarflow.components;

import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface IAccumulatorLengthFunction<I> {

    @Suspendable
    Double apply(I i);
}
