package it.enryold.quasarflow.interfaces;

public interface IFlowable {

    void start();
    void destroy();
    IFlow flow();
}
