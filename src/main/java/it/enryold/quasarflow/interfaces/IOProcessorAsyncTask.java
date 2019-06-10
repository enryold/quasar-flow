package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.SendPort;
import org.apache.http.nio.client.methods.AsyncCharConsumer;

@FunctionalInterface
public interface IOProcessorAsyncTask<I, O> {

    @Suspendable
    void async(I elm, SendPort<O> sendPort);
}

