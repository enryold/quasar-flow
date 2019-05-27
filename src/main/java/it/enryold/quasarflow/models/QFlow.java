package it.enryold.quasarflow.models;

import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.abstracts.AbstractFlow;
import it.enryold.quasarflow.models.utils.QMetric;
import it.enryold.quasarflow.models.utils.QSettings;


public class QFlow extends AbstractFlow {

    public QFlow(){}
    public QFlow(QSettings settings, Channel<QMetric> metricChannel){
        super(settings, metricChannel);
    }
}
