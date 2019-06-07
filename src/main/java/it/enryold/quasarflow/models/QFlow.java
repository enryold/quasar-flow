package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractFlow;
import it.enryold.quasarflow.models.utils.QSettings;


public class QFlow extends AbstractFlow {

    public QFlow(){}
    public QFlow(QSettings settings){
        super(settings);
    }
}
