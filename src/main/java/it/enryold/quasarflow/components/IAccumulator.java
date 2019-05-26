package it.enryold.quasarflow.components;

import java.util.List;

public interface IAccumulator<I, T>  {


    boolean add(I obj);
    List<T> getRecords();
    IAccumulatorLengthFunction<I> accumulatorLengthFunction();

}
