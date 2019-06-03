package it.enryold.quasarflow.models.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QMetricComponent {

    private String componentName;
    private String flowName;
    private Map<String, QMetric> metrics = new HashMap<>();

    public QMetricComponent(String flowName, String componentName){
        this.flowName = flowName;
        this.componentName = componentName;
    }

    public Set<QMetric> getMetrics() {
        return metrics.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    public void setMetrics(Set<QMetric> metrics) {
        metrics.forEach(this::addMetric);
    }

    private void addMetric(QMetric metric) {

        QMetric stored = metrics.get(metric.getMetricName());

        if(stored == null){
            stored = metric;
        }else {
            stored.setValue(stored.getValue()+metric.getValue());
        }

        this.metrics.put(stored.getMetricName(), stored);
    }

    public String getComponentName() {
        return componentName;
    }

    public String getFlowName() {
        return flowName;
    }


    @Override
    public String toString() {
        return "QMetric: "+componentName+" "+ metrics.values().stream().map(m -> m.getMetricName()+": "+m.getValue()).collect(Collectors.joining(" | "));
    }

    @Override
    public int hashCode() {
        return (componentName+flowName).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }
}
