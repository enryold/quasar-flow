package it.enryold.quasarflow.kinesis.streams.producer.accumulators;

import it.enryold.quasarflow.components.IAccumulator;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic.BasicKinesisStreamsAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic.BasicKinesisStreamsByteMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic.BasicKinesisStreamsRecordMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic.KinesisStreamsAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsByteMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsRecordMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsStringMapper;
import it.enryold.quasarflow.kinesis.streams.producer.models.KinesisStreamsMapper;

import java.nio.ByteBuffer;

public class KinesisStreamsAccumulatorFactory<I> implements IAccumulatorFactory<I, ByteBuffer> {

    private IKinesisStreamsStringMapper<I> stringMapper;
    private IKinesisStreamsRecordMapper recordMapper;
    private IKinesisStreamsByteMapper byteMapper;
    private KinesisStreamsAccumulatorLengthFunction<I> accumulatorLengthFunction;
    private double byteSizeLimit;

    public KinesisStreamsAccumulatorFactory(double byteSizeLimit,
                                            IKinesisStreamsStringMapper<I> stringMapper,
                                            IKinesisStreamsRecordMapper recordMapper,
                                            IKinesisStreamsByteMapper byteMapper,
                                            KinesisStreamsAccumulatorLengthFunction<I> accumulatorLengthFunction){
        this.stringMapper = stringMapper;
        this.recordMapper = recordMapper;
        this.byteMapper = byteMapper;
        this.byteSizeLimit = byteSizeLimit;
        this.accumulatorLengthFunction = accumulatorLengthFunction;
    }


    public static <I> KinesisStreamsAccumulatorFactory<I> getBasicRecordFactory(double byteSizeLimit, IKinesisStreamsStringMapper<I> stringMapper){
        return new KinesisStreamsAccumulatorFactory<>(byteSizeLimit, stringMapper, new BasicKinesisStreamsRecordMapper(), new BasicKinesisStreamsByteMapper(), new BasicKinesisStreamsAccumulatorLengthFunction<>());
    }



    @Override
    public IAccumulator<I, ByteBuffer> build() {

        KinesisStreamsMapper mapper = KinesisStreamsMapper.newBuilder()
                .withByteMapper(byteMapper)
                .withRecordMapper(recordMapper)
                .withStringMapper(stringMapper)
                .build();
        accumulatorLengthFunction
                .withFirehoseMapper(mapper);

        return new KinesisStreamsAccumulator<I>(byteSizeLimit, mapper, accumulatorLengthFunction);

    }
}
