package it.enryold.quasarflow.kinesis.streams.producer.hashproviders;


import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitHashProvider;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IStreamShardHelperFactory;

public class HashProvider implements IExplicitHashProvider {


    private final IStreamShardHelperFactory streamShardHelperFactory;


    public HashProvider(IStreamShardHelperFactory streamShardHelperFactory)
    {
        this.streamShardHelperFactory = streamShardHelperFactory;
    }

    @Override
    public String nextHashKey() {
        return streamShardHelperFactory.build().keyHelperForStream().generateHashKey();
    }
}
