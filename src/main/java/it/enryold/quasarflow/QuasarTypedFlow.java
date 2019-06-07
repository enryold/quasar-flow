package it.enryold.quasarflow;

import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.models.QFlow;
import it.enryold.quasarflow.models.utils.QSettings;

public class QuasarTypedFlow<T> {


    private IFlow qFlow;
    private IEmitter<T> inputEmitter;

    private QuasarTypedFlow(){
        qFlow = new QFlow();
        inputEmitter = new QEmitter<>(qFlow);
    }
    private QuasarTypedFlow(QSettings settings){
        qFlow = new QFlow(settings);
        inputEmitter = new QEmitter<T>(qFlow).broadcastEmitter(publisherChannel -> { });
    }


    public static <T> QuasarTypedFlow<T> newFlow(){
        return new QuasarTypedFlow<>();
    }
    public static <T> QuasarTypedFlow<T> newFlow(QSettings settings){
        return new QuasarTypedFlow<>(settings);
    }



    public IEmitter<T> getEmitter(){
        return inputEmitter;
    }

}
