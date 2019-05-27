package it.enryold.quasarflow.models.utils;

import it.enryold.quasarflow.interfaces.IFlowable;

import java.util.function.BiFunction;

public class FnBuildMetric implements BiFunction<IFlowable, String, QMetric> {
    @Override
    public QMetric apply(IFlowable iFlowable, String metricName) {
        return QMetric.Builder()
                .withMetricName(metricName)
                .withComponentName(iFlowable.getClass().getSimpleName())
                .withFlowName(iFlowable.flow().getName())
                .build();
    }
}
