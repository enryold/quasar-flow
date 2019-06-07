package it.enryold.quasarflow.interfaces;

import it.enryold.quasarflow.models.utils.QSettings;

public interface IFlow {


    void addStartable(IFlowable flowable);

    void setParentNested();
    void setNested(IFlowable flowable);
    QSettings getSettings();
    String getName();
    <I extends IFlow> I start();
    void destroy();
    void print();
    void printMetrics();

}
