package it.enryold.quasarflow.models.metrics;

public class FnBuildMetric {

    public QMetric create(String metricName, Long value) {
        return QMetric.Builder()
                .withMetricName(metricName)
                .withValue(value)
                .build();
    }
}
