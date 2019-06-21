package it.enryold.quasarflow.interfaces;


import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.models.utils.QEmitterChannel;

@FunctionalInterface
public interface IEmitterTask<T> {

    @Suspendable
    void emitOn(QEmitterChannel<T> publisherChannel) throws InterruptedException, SuspendExecution;
}
