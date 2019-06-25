package it.enryold.quasarflow.components;

import co.paralleluniverse.fibers.Suspendable;

import java.util.List;

public interface IAccumulator<I, T>  {


    @Suspendable
    boolean add(I obj);

    @Suspendable
    List<T> getRecords();

    @Suspendable
    IAccumulatorLengthFunction<I> accumulatorLengthFunction();

}
