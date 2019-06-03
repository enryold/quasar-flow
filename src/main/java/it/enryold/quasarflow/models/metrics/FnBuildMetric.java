package it.enryold.quasarflow.models.metrics;

import it.enryold.quasarflow.interfaces.IFlowable;

import java.util.function.BiFunction;

public class FnBuildMetric {

    public QMetric create(IFlowable iFlowable, String metricName, Long value) {
        return QMetric.Builder()
                .withMetricName(metricName)
                .withComponentName(iFlowable.getName())
                .withFlowName(iFlowable.flow().getName())
                .withValue(value)
                .build();
    }
}
