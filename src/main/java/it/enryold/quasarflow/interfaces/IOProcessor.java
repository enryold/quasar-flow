package it.enryold.quasarflow.interfaces;

public interface IOProcessor<I, O> extends IFlowable {


    IOProcessor<I, O> withAsyncTaskBuilder(IOProcessorTaskBuilder<I, O> processorAsyncTaskBuilder);
    <EM extends IEmitter<O>> EM process();
    IEmitterList<O> processWithFanOut(int workers);
    <EM extends IEmitter<O>> EM processWithFanIn(int workers);


    void destroy();
}

