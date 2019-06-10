package it.enryold.quasarflow.components;

import co.paralleluniverse.fibers.Suspendable;

public interface IAccumulatorFactory<I, T>  {

    @Suspendable
    IAccumulator<I, T> build();
}
