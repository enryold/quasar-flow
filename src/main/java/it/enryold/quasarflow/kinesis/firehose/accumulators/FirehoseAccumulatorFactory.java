package it.enryold.quasarflow.kinesis.firehose.accumulators;

import com.amazonaws.services.kinesisfirehose.model.Record;
import it.enryold.quasarflow.components.IAccumulator;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.kinesis.firehose.accumulators.generic.BasicFirehoseAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.firehose.accumulators.generic.BasicFirehoseByteMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.generic.BasicFirehoseRecordMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.generic.FirehoseAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseByteMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseRecordMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseStringMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.smartGZIP.SmartGZIPFirehoseAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.firehose.accumulators.smartGZIP.SmartGZIPFirehoseRecordMapper;
import it.enryold.quasarflow.kinesis.firehose.models.FirehoseMapper;

public class FirehoseAccumulatorFactory<I> implements IAccumulatorFactory<I, Record> {

    private IFirehoseStringMapper<I> stringMapper;
    private IFirehoseRecordMapper recordMapper;
    private IFirehoseByteMapper byteMapper;
    private FirehoseAccumulatorLengthFunction<I> accumulatorLengthFunction;
    private double byteSizeLimit;

    public FirehoseAccumulatorFactory(double byteSizeLimit, IFirehoseStringMapper<I> stringMapper, IFirehoseRecordMapper recordMapper, IFirehoseByteMapper byteMapper, FirehoseAccumulatorLengthFunction<I> firehoseAccumulatorLengthFunction){
        this.stringMapper = stringMapper;
        this.recordMapper = recordMapper;
        this.byteMapper = byteMapper;
        this.byteSizeLimit = byteSizeLimit;
        this.accumulatorLengthFunction = firehoseAccumulatorLengthFunction;
    }


    public static <I> FirehoseAccumulatorFactory<I> getSmartGZIPFactory(double byteSizeLimit, IFirehoseStringMapper<I> stringMapper){
        return new FirehoseAccumulatorFactory<>(byteSizeLimit, stringMapper, new SmartGZIPFirehoseRecordMapper(), new BasicFirehoseByteMapper(), new SmartGZIPFirehoseAccumulatorLengthFunction<>());
    }

    public static <I> FirehoseAccumulatorFactory<I> getBasicRecordFactory(double byteSizeLimit, IFirehoseStringMapper<I> stringMapper){
        return new FirehoseAccumulatorFactory<>(byteSizeLimit, stringMapper, new BasicFirehoseRecordMapper(), new BasicFirehoseByteMapper(), new BasicFirehoseAccumulatorLengthFunction<>());
    }



    @Override
    public IAccumulator<I, Record> build() {

        FirehoseMapper<I> mapper = FirehoseMapper.<I>newBuilder()
                .withByteMapper(byteMapper)
                .withRecordMapper(recordMapper)
                .withStringMapper(stringMapper)
                .build();
        accumulatorLengthFunction
                .withFirehoseMapper(mapper);

        return new FirehoseAccumulator<>(byteSizeLimit, mapper, accumulatorLengthFunction);

    }
}
