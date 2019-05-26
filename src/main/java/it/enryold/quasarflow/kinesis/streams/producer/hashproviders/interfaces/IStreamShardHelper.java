package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces;


public interface IStreamShardHelper {

    String getStreamName();
    IExplicitShardKeyHelper keyHelperForStream();
}
