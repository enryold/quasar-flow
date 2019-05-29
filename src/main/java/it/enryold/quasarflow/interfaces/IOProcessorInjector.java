package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface IOProcessorInjector<I, O, S extends IEmitter<O>> {

    S inject(IOProcessor<I, O> emitter);
}
