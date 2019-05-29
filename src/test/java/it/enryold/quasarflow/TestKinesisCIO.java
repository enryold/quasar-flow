//package it.enryold.quasarflow;
//
//import co.paralleluniverse.strands.Strand;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.kinesis.AmazonKinesis;
//import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
//import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
//import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
//import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel;
//import com.amazonaws.services.kinesis.producer.KinesisProducer;
//import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
//import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
//import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClientBuilder;
//import it.enryold.quasarflow.interfaces.IEmitterTask;
//import it.enryold.quasarflow.interfaces.IConsumerTaskFactory;
//import it.enryold.quasarflow.kinesis.firehose.models.KFConsumerTaskFactory;
//import it.enryold.quasarflow.kinesis.firehose.accumulators.FirehoseAccumulatorFactory;
//import it.enryold.quasarflow.kinesis.firehose.accumulators.generic.BasicFirehoseJsonStringMapper;
//import it.enryold.quasarflow.kinesis.streams.KSQFlow;
//import it.enryold.quasarflow.kinesis.streams.models.KPLConsumerTaskFactory;
//import it.enryold.quasarflow.kinesis.streams.common.KinesisJsonListDecoder;
//import it.enryold.quasarflow.kinesis.streams.consumer.v1.RecordProcessorFactory;
//import it.enryold.quasarflow.kinesis.streams.producer.accumulators.KinesisStreamsAccumulatorFactory;
//import it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic.BasicKinesisStreamsJsonStringMapper;
//import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.HashProviderFactory;
//import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.roundrobin.RRStreamShardHelperFactory;
//import it.enryold.quasarflow.models.User;
//import org.junit.jupiter.api.Test;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//public class TestKinesisCIO extends TestUtils{
//
//
//    private BasicAWSCredentials credentials = new BasicAWSCredentials(
//            "ACCESS_KEY",
//            "SECRET");
//    private String applicationName = "KINESIS_APPLICATION_NAME";
//    private String inputStreamName = "KINESIS_STREAM_NAME";
//    private String region = "AWS_REGION";
//    private String kinesisMetricLevel = "none";
//
//
//    private KinesisClientLibConfiguration kclConfiguration()
//    {
//        return new KinesisClientLibConfiguration(applicationName,
//                inputStreamName,
//                new AWSStaticCredentialsProvider(credentials),
//                "WORKER-LOCAL"+ UUID.randomUUID().toString())
//                .withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON)
//                .withIdleTimeBetweenReadsInMillis(500)
//                .withCallProcessRecordsEvenForEmptyRecordList(true)
//                .withParentShardPollIntervalMillis(10000)
//                .withLogWarningForTaskAfterMillis(1000)
//                .withMetricsLevel(MetricsLevel.NONE)
//                .withMetricsBufferTimeMillis(300000)
//                .withMaxLeasesForWorker(Integer.MAX_VALUE);
//    }
//
//    private KinesisProducer kinesisProducer(){
//        KinesisProducerConfiguration config = new KinesisProducerConfiguration()
//                .setRecordMaxBufferedTime(60000)
//                .setMaxConnections(24)
//                .setRequestTimeout(10000)
//                .setRecordTtl(180000)
//                .setCredentialsProvider(new AWSStaticCredentialsProvider(credentials))
//                .setMetricsLevel(kinesisMetricLevel)
//                .setRegion(region);
//
//        return new KinesisProducer(config);
//    }
//
//
//    @Test
//    public void testKinesisFirehose() throws InterruptedException {
//
//        IEmitterTask<User> userEmitter = publisherChannel -> { for(User usr : userGenerator(2_000_000)){ publisherChannel.send(usr); } };
//
//        AmazonKinesisFirehose firehoseClient = AmazonKinesisFirehoseClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .build();
//
//        FirehoseAccumulatorFactory<User> accumulatorFactory = FirehoseAccumulatorFactory
//                .getBasicRecordFactory(100, new BasicFirehoseJsonStringMapper<>());
//
//        KFConsumerTaskFactory receiverTaskFactory = new KFConsumerTaskFactory(
//                firehoseClient,
//                Collections.singletonList("FIREHOSE_STREAM_NAME"),
//                true);
//
//        // FIREHOSE FLOW
//        QuasarFlow.newFlow()
//                .broadcastEmitter(userEmitter)
//                .addConsumer(r -> r.consumeWithFanOutAndByteBatching(4, accumulatorFactory, 10, TimeUnit.SECONDS, receiverTaskFactory))
//                .flow()
//                .start();
//
//        Thread.sleep(100_000);
//
//    }
//
//
//    @Test
//    public void testKPL_KCL() throws InterruptedException {
//
//        IEmitterTask<User> userEmitter = publisherChannel -> { for(User usr : userGenerator(2_000_000)){ publisherChannel.send(usr); } };
//
//        AmazonKinesis amazonKinesis = AmazonKinesisClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .build();
//
//        KinesisProducer kinesisProducer = kinesisProducer();
//
//        // KPL QConsumer Task
//        KPLConsumerTaskFactory receiverTaskFactory = KPLConsumerTaskFactory.Builder()
//                .withPartitionKey("PARTITION_KEY")
//                .withStreamName(inputStreamName)
//                .withHashProviderFactory(new HashProviderFactory(amazonKinesis, inputStreamName, new RRStreamShardHelperFactory()))
//                .withKinesisProducer(kinesisProducer)
//                .build();
//
//        // Kinesis Bytebuffer accumulator (500KB)
//        KinesisStreamsAccumulatorFactory<User> accumulatorFactory = KinesisStreamsAccumulatorFactory
//                .getBasicRecordFactory(500, new BasicKinesisStreamsJsonStringMapper<>());
//
//
//        // KPL FLOW
//        QuasarFlow.newFlow()
//                .broadcastEmitter(userEmitter)
//                .addConsumer(r -> r.consumeWithFanOutAndByteBatching(4, accumulatorFactory, 10, TimeUnit.SECONDS, receiverTaskFactory))
//                .flow()
//                .start();
//
//
//        // KCL
//        RecordProcessorFactory recordProcessorFactory = new RecordProcessorFactory()
//                .withBackoffTime(500)
//                .withCheckpointInterval(10000)
//                .withRetriesNumber(10);
//
//
//        KSQFlow.newFlow(kclConfiguration())
//                .kinesisEmitter(recordProcessorFactory)
//                .addProcessor("PARTITION_KEY", sub -> sub
//                        .process(new KinesisJsonListDecoder<>(User.class))
//                        .addConsumer(rec -> rec.consumeWithFanOutAndSizeBatching(4, 100, 10, TimeUnit.SECONDS,
//                                (IConsumerTaskFactory<List<List<User>>>) () -> elm -> System.out.println(Strand.currentStrand().getName()+" - List received with size: "+elm.size()))))
//                .flow()
//                .start();
//
//
//        Thread.sleep(100_000);
//    }
//
//
//
//}
