package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface InjectorEmitter<T, O> {

    IEmitter<O> injectEmitter(IEmitter<T> emitter);
}
