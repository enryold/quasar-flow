package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces;

public interface IExplicitShardKeyHelper {

    String generateHashKey();
}
