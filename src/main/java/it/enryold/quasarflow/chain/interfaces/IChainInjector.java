package it.enryold.quasarflow.chain.interfaces;

import co.paralleluniverse.fibers.Suspendable;
import it.enryold.quasarflow.chain.FiberChain;

@FunctionalInterface
public interface IChainInjector<E> {

    @Suspendable
    void inject(IChain<E> chain);
}
