package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.roundrobin;


import co.paralleluniverse.fibers.Suspendable;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitShardKeyHelper;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IStreamShardHelper;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IStreamShardHelperFactory;

import java.util.Set;

import static com.google.common.collect.Iterators.cycle;

public class RRStreamShardHelperFactory implements IStreamShardHelperFactory {

    private Set<IExplicitShardKeyHelper> helpers;
    private String streamName;



    @Override
    public IStreamShardHelperFactory withHelpers(Set<IExplicitShardKeyHelper> helpers) {
        this.helpers = helpers;
        return this;
    }

    @Override
    public IStreamShardHelperFactory withStreamName(String streamName) {
        this.streamName = streamName;
        return this;
    }

    @Override
    public IStreamShardHelper build() {
        return new RRStreamShardHelper(cycle(helpers), streamName);
    }
}
