package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface InjectorConsumer<T> {

    void injectConsumer(IEmitter<T> emitter);
}
