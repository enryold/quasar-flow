package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.SendPort;
import org.apache.http.nio.client.methods.AsyncCharConsumer;

@FunctionalInterface
public interface IOProcessorTask<I, O> {

    @Suspendable
    O sync(I elm);
}

