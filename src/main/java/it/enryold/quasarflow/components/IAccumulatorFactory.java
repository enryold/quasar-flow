package it.enryold.quasarflow.components;

public interface IAccumulatorFactory<I, T>  {


    IAccumulator<I, T> build();
}
