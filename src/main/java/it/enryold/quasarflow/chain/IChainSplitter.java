package it.enryold.quasarflow.chain;

import co.paralleluniverse.fibers.Suspendable;

import java.util.List;

@FunctionalInterface
public interface IChainSplitter<I> {

    @Suspendable
    List<I> split(I input);
}
