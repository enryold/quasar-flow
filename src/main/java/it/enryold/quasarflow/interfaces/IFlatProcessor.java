package it.enryold.quasarflow.interfaces;

public interface IFlatProcessor<I> extends IFlowable {



    <EM extends IEmitter<I>> EM process();
    IEmitterList<I> processWithFanOut(int workers);
    <EM extends IEmitter<I>> EM processWithFanIn(int workers);




    void destroy();
}

