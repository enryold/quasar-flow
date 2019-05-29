package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface IOProcessorAsyncTaskBuilder<I, O> {

    IOProcessorAsyncTask<I, O> build();
}

