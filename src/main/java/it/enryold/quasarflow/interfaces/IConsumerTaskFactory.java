package it.enryold.quasarflow.interfaces;



@FunctionalInterface
public interface IConsumerTaskFactory<I> {

    IConsumerTask<I> build();
}
