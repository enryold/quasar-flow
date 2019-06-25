package it.enryold.quasarflow.models.utils;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;

import java.util.function.Consumer;

public class QEmitterChannel<T> {

    private Channel<T> publisherChannel;
    private Consumer<T> afterSendCallback;


    public QEmitterChannel(Channel<T> channel){
        this.publisherChannel = channel;
    }

    public QEmitterChannel(Channel<T> channel, Consumer<T> afterSendCallback){
        this.publisherChannel = channel;
        this.afterSendCallback = afterSendCallback;
    }


    public void sendOnChannel(T obj) throws InterruptedException, SuspendExecution {
        publisherChannel.send(obj);
        if(afterSendCallback != null){
            afterSendCallback.accept(obj);
        }
    }
}
