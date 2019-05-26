package it.enryold.quasarflow.kinesis.streams.producer.hashproviders;

import com.amazonaws.services.kinesis.AmazonKinesis;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.abstracts.AbstractHashProviderFactory;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitHashProvider;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IStreamShardHelperFactory;


public class HashProviderFactory extends AbstractHashProviderFactory {


    public HashProviderFactory(AmazonKinesis amazonKinesis, String streamName, IStreamShardHelperFactory factory) {
        super(amazonKinesis, streamName, factory);
    }

    @Override
    public IExplicitHashProvider getHashProvider() {
        return new HashProvider(shardKeyHelperFactory);
    }
}
