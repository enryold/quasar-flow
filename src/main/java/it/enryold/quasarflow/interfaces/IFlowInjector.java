package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface IFlowInjector<T, O> {

    IEmitter<O> inject(IEmitter<T> emitter);
}
