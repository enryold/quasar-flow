package it.enryold.quasarflow;

import it.enryold.quasarflow.interfaces.IRoutingKeyExtractor;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.models.QFlow;
import it.enryold.quasarflow.models.utils.QMetric;
import it.enryold.quasarflow.models.utils.QSettings;

public class MetricFlow {


    private QFlow qFlow;

    private MetricFlow(){
        qFlow = new QFlow();
    }
    private MetricFlow(QSettings settings){
        qFlow = new QFlow(settings, null);
    }


    public static MetricFlow newFlow(){
        return new MetricFlow();
    }
    public static MetricFlow newFlow(QSettings settings){
        return new MetricFlow(settings);
    }


    public QEmitter<QMetric> metricEmitter(){
        return new QEmitter<QMetric>(qFlow)
                .broadcastEmitter(null);
    }

    public QEmitter<QMetric> metricEmitter(IRoutingKeyExtractor<QMetric> routingKeyExtractor){
        return new QEmitter<QMetric>(qFlow)
                .routedEmitter(null, routingKeyExtractor);
    }


}
