package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface ITransformFactory<I, T> {

    @Suspendable
    ITransform<I, T> build();
}
