package it.enryold.quasarflow.chain.interfaces;

import co.paralleluniverse.fibers.Suspendable;
import it.enryold.quasarflow.chain.FiberChain;

public interface IChain<E> {

    @Suspendable
    E getObject();

    @Suspendable
    <T> FiberChain<T> transform(String name, IChainFunction<E, T> fn);

    @Suspendable
    void consume(String name, IChainConsumer<E> fn);

    @Suspendable
    void consume(String name, IChainConsumer<E> fn, IChainSplitter<E> splitter);
}
