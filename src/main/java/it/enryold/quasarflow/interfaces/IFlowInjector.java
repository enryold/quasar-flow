package it.enryold.quasarflow.interfaces;

@FunctionalInterface
public interface IFlowInjector<O extends IFlowable> {

    O inject(IFlow flow);
}
