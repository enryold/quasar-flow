package it.enryold.quasarflow.models.metrics;

import it.enryold.quasarflow.abstracts.AbstractAccumulator;
import it.enryold.quasarflow.components.IAccumulatorLengthFunction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QMetricAccumulator extends AbstractAccumulator<QMetric, QMetricComponent> {


    public QMetricAccumulator(double byteSizeLimit) {
        super(byteSizeLimit);
    }

    @Override
    public List<QMetricComponent> getRecords() {

        return accumulator
                .stream()
                .collect(Collectors.groupingBy(m -> new QMetricComponent(m.getFlowName(), m.getComponentName()), Collectors.toList()))
                .entrySet()
                .stream()
                .peek(e -> e.getKey().setMetrics(new HashSet<>(e.getValue())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }

    @Override
    public IAccumulatorLengthFunction<QMetric> accumulatorLengthFunction() {
        return metric -> 1.0;
    }
}
