package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.models.metrics.QMetric;

public interface IFlowable<T> {

    <I extends IFlowable<T>> I withMetricChannel(Channel<QMetric> metricChannel);
    void setName(String name);
    String getName();
    void start();
    void destroy();
    IFlow flow();
}
