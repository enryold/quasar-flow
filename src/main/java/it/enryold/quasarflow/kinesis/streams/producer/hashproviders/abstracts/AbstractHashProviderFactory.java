package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.abstracts;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallExecutorBuilder;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.utils.ExplicitShardKeyHelper;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitHashProviderFactory;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitShardKeyHelper;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IStreamShardHelperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public abstract class AbstractHashProviderFactory implements IExplicitHashProviderFactory {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private AmazonKinesis amazonKinesis;
    private String streamName;
    protected IStreamShardHelperFactory shardKeyHelperFactory;


    public AbstractHashProviderFactory(AmazonKinesis amazonKinesis, String streamName, IStreamShardHelperFactory iStreamShardHelperFactory) {
        this.amazonKinesis = amazonKinesis;
        this.streamName = streamName;
        this.shardKeyHelperFactory = iStreamShardHelperFactory;
        this.init();
    }


    protected void init() {
        log.info("KCL Describe stream "+streamName+" init!");

        Callable<Set<IExplicitShardKeyHelper>> callable = () -> amazonKinesis.describeStream(streamName).getStreamDescription()
               .getShards()
               .stream()
               .peek(s -> log.info("Shard "+s.getShardId()+" on " + streamName + ", range: " + s.getHashKeyRange()))
               .map(s -> new ExplicitShardKeyHelper(s.getShardId(), s.getHashKeyRange()))
               .collect(Collectors.toSet());

        RetryConfig config = new RetryConfigBuilder()
                .exponentialBackoff5Tries5Sec()
                .build();

        CallExecutor<Set<IExplicitShardKeyHelper>> executor = new CallExecutorBuilder<>()
                .config(config)
                .onSuccessListener(status -> log.info("Describe stream "+streamName+" success!"))
                .onFailureListener(status -> log.info("Describe stream "+streamName+" failed!"))
                .build();

        Set<IExplicitShardKeyHelper> shardHelpers = executor.execute(callable).getResult();

        shardKeyHelperFactory.withHelpers(shardHelpers)
                .withStreamName(streamName);
    }

}
