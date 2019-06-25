package it.enryold.quasarflow.chain.interfaces;

import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface IChainFunction<I, O> {

    @Suspendable
    O apply(I i);
}
