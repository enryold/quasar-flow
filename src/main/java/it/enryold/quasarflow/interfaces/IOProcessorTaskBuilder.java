package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface IOProcessorTaskBuilder<I, O> {

    IOProcessorTask<I, O> build();
}

