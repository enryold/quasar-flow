package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface ITransformFactory<I, T> {

    ITransform<I, T> build();
}
