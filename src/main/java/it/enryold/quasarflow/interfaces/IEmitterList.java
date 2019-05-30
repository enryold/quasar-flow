package it.enryold.quasarflow.interfaces;



public interface IEmitterList<I> {

    IFlow flow();
    void setName(String name);
    IFlow cycle(Injector<IEmitter<I>> injector);
}
