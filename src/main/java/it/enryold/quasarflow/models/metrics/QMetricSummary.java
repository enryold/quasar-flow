package it.enryold.quasarflow.models.metrics;

import java.util.*;
import java.util.function.Consumer;

public class QMetricSummary {


    private Date creation = new Date();
    private Set<QMetricComponent> metrics;


    public QMetricSummary(List<QMetricComponent> metricComponents){
        this.metrics = new HashSet<>(metricComponents);
    }


    public void printSummary(Consumer<String> logger){


        logger.accept("QMetric: "+creation+"\n");
        metrics
                .stream()
                .sorted(Comparator.comparing(QMetricComponent::hashCode))
                .forEach(m -> logger.accept(m.toString()));
    }

}
