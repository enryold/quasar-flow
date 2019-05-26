package it.enryold.quasarflow.interfaces;

public interface IFlow {

    void addStartable(IFlowable startable);
    IFlow start();
    void destroy();
}
