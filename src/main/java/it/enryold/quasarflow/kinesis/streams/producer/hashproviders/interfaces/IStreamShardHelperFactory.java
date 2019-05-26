package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces;


import java.util.Set;

public interface IStreamShardHelperFactory {

    IStreamShardHelperFactory withHelpers(Set<IExplicitShardKeyHelper> helpers);
    IStreamShardHelperFactory withStreamName(String streamName);
    IStreamShardHelper build();

}
