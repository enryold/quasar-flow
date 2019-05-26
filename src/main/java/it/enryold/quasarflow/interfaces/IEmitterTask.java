package it.enryold.quasarflow.interfaces;


import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;

@FunctionalInterface
public interface IEmitterTask<T> {

    void emit(Channel<T> publisherChannel) throws InterruptedException, SuspendExecution;
}
