package it.enryold.quasarflow.interfaces;



@FunctionalInterface
public interface IConsumerTask<I> {

    void ingest(I elm);
}
