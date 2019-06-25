package it.enryold.quasarflow.chain;

import co.paralleluniverse.fibers.Suspendable;

public interface IChain<E> {

    @Suspendable
    <T> FiberChain<T> transform(String name, IChainFunction<E, T> fn);

    @Suspendable
    void consume(String name, IChainConsumer<E> fn);

    @Suspendable
    void consume(String name, IChainConsumer<E> fn, IChainSplitter<E> splitter);
}
