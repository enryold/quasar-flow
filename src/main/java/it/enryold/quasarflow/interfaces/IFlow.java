package it.enryold.quasarflow.interfaces;

import it.enryold.quasarflow.models.QSettings;

public interface IFlow {

    void addStartable(IFlowable startable);
    QSettings getSettings();
    IFlow start();
    void destroy();
}
