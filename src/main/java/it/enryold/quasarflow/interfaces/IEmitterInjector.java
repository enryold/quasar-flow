package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface IEmitterInjector<T, O extends IFlowable<T>> {

    O inject(IEmitter<T> emitter);
}
