package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces;

import co.paralleluniverse.fibers.Suspendable;

public interface IExplicitHashProviderFactory {

    @Suspendable
    IExplicitHashProvider getHashProvider();
}
