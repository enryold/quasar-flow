package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.fibers.Suspendable;


@FunctionalInterface
public interface ITransform<I, T> {

    @Suspendable
    T apply(I i);
}
