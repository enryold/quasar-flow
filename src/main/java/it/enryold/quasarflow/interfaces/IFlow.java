package it.enryold.quasarflow.interfaces;

import it.enryold.quasarflow.models.utils.QSettings;

public interface IFlow {


    void addStartable(IFlowable startable);
    QSettings getSettings();
    String getName();
    <I extends IFlow> I start();
    void destroy();

}
