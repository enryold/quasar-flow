package it.enryold.quasarflow.interfaces;

public interface IFlowable {

    void setName(String name);
    String getName();
    void start();
    void destroy();
    IFlow flow();
    IFlowable parent();

}
