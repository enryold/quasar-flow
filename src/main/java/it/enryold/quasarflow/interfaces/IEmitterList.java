package it.enryold.quasarflow.interfaces;



public interface IEmitterList<I> {

    IFlow flow();
    IFlow cycle(Injector<IEmitter<I>> injector);
}
