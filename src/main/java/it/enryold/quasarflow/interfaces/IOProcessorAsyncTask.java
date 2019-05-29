package it.enryold.quasarflow.interfaces;

import co.paralleluniverse.strands.channels.SendPort;
import org.apache.http.nio.client.methods.AsyncCharConsumer;

@FunctionalInterface
public interface IOProcessorAsyncTask<I, O> {

    void async(I elm, SendPort<O> sendPort);
}

