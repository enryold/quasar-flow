package it.enryold.quasarflow.chain.interfaces;

import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface IChainConsumer<I> {

    @Suspendable
    void consume(I input);
}
