package it.enryold.quasarflow.interfaces;

import it.enryold.quasarflow.models.metrics.QMetric;

import java.util.List;

public interface IFlowable {

    void setName(String name);
    List<QMetric> getMetrics();
    String getName();
    void start();
    void destroy();
    IFlow flow();
    IFlowable parent();

}
