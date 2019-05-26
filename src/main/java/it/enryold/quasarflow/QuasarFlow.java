package it.enryold.quasarflow;

import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IRoutingKeyExtractor;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.models.QFlow;

public class QuasarFlow {


    private QFlow qFlow;

    private QuasarFlow(){
        qFlow = new QFlow();
    }

    public static QuasarFlow newFlow(){
        return new QuasarFlow();
    }

    public <T, E extends IEmitter<T>> E broadcastEmitter(IEmitterTask<T> task){
        return new QEmitter<T>(qFlow)
                .broadcastEmitter(task);
    }

    public <T, E extends IEmitter<T>> E broadcastEmitter(IEmitterTask<T> task, String name){
        return new QEmitter<T>(qFlow, name)
                .broadcastEmitter(task);
    }

    public <T, E extends IEmitter<T>> E routedEmitter(IEmitterTask<T> task, IRoutingKeyExtractor<T> routingKeyExtractor){
        return new QEmitter<T>(qFlow)
                .routedEmitter(task, routingKeyExtractor);
    }

    public <T, E extends IEmitter<T>> E routedEmitter(IEmitterTask<T> task, String name, IRoutingKeyExtractor<T> routingKeyExtractor){
        return new QEmitter<T>(qFlow, name)
                .routedEmitter(task, routingKeyExtractor);
    }

}
