package it.enryold.quasarflow;

import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.models.QFlow;
import it.enryold.quasarflow.models.utils.QMetric;
import it.enryold.quasarflow.models.utils.QSettings;

public class QuasarFlow {


    private IFlow qFlow;

    private QuasarFlow(){
        qFlow = new QFlow();
    }
    private QuasarFlow(QSettings settings, Channel<QMetric> metricChannel){
        qFlow = new QFlow(settings, metricChannel);
    }


    public static QuasarFlow newFlow(){
        return new QuasarFlow();
    }
    public static QuasarFlow newFlow(QSettings settings){
        return new QuasarFlow(settings, null);
    }
    public static QuasarFlow newFlow(QSettings settings, Channel<QMetric> metricChannel){
        return new QuasarFlow(settings, metricChannel);
    }



    public <T, E extends IEmitter<T>> E withEmitter(IEmitter<T> emitter){
        this.qFlow = emitter.flow();
        return (E)emitter;
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
