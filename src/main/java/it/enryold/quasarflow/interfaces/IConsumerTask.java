package it.enryold.quasarflow.interfaces;


import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface IConsumerTask<I> {

    @Suspendable
    void ingest(I elm);
}
