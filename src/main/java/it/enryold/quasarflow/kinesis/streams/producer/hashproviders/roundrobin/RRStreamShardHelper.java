package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.roundrobin;


import co.paralleluniverse.fibers.Suspendable;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitShardKeyHelper;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IStreamShardHelper;

import java.util.Iterator;

public class RRStreamShardHelper implements IStreamShardHelper {

    private Iterator<IExplicitShardKeyHelper> iterator;
    private String streamName;


    public RRStreamShardHelper(Iterator<IExplicitShardKeyHelper> iterator, String streamName) {
        this.iterator = iterator;
        this.streamName = streamName;
    }

    @Override
    public String getStreamName() {
        return streamName;
    }

    public IExplicitShardKeyHelper keyHelperForStream(){
        return iterator.next();
    }

}
