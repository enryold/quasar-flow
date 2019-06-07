package it.enryold.quasarflow;

import co.paralleluniverse.fibers.SuspendExecution;
import it.enryold.quasarflow.interfaces.IConsumerTask;
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
        inputEmitter = new QEmitter<>(qFlow, qFlow.getName()+"Emitter");
    }
    private QuasarTypedFlow(QSettings settings){
        qFlow = new QFlow(settings);
        inputEmitter = new QEmitter<T>(qFlow, qFlow.getName()+"Emitter").broadcastEmitter(publisherChannel -> { });
    }

    private QuasarTypedFlow(String name, QSettings settings){
        qFlow = new QFlow(name, settings);
        inputEmitter = new QEmitter<T>(qFlow, qFlow.getName()+"Emitter").broadcastEmitter(publisherChannel -> { });
    }


    public static <T> QuasarTypedFlow<T> newFlow(){
        return new QuasarTypedFlow<>();
    }
    public static <T> QuasarTypedFlow<T> newFlow(QSettings settings){
        return new QuasarTypedFlow<>(settings);
    }
    public static <T> QuasarTypedFlow<T> newFlow(String name, QSettings settings){
        return new QuasarTypedFlow<>(name, settings);
    }



    public IEmitter<T> getEmitter(){
        return inputEmitter;
    }
    public IEmitter<T> getEmitter(String name){
        inputEmitter.setName(name);
        return inputEmitter;
    }

    public IConsumerTask<T> buildConsumerTask(){
        return elm -> {
            try {
                getEmitter().getChannel().send(elm);
            } catch (SuspendExecution | InterruptedException suspendExecution) {
                suspendExecution.printStackTrace();
            }
        };
    }

    public IFlow getFlow(){ return qFlow; }

}
