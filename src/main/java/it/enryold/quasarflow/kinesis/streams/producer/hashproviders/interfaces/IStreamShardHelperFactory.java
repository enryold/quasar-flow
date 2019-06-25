package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces;


import co.paralleluniverse.fibers.Suspendable;

import java.util.Set;

public interface IStreamShardHelperFactory {

    IStreamShardHelperFactory withHelpers(Set<IExplicitShardKeyHelper> helpers);
    IStreamShardHelperFactory withStreamName(String streamName);

    @Suspendable
    IStreamShardHelper build();

}
