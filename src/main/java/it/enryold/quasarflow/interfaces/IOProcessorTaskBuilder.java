package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface IOProcessorTaskBuilder<I, O> {

    @Suspendable
    IOProcessorTask<I, O> build();
}

