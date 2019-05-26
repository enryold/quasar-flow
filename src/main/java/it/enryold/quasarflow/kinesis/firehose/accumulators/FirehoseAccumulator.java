package it.enryold.quasarflow.kinesis.firehose.accumulators;

import com.amazonaws.services.kinesisfirehose.model.Record;
import it.enryold.quasarflow.abstracts.AbstractAccumulator;
import it.enryold.quasarflow.components.IAccumulatorLengthFunction;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseByteMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseRecordMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseStringMapper;
import it.enryold.quasarflow.kinesis.firehose.models.FirehoseMapper;

import java.util.List;

public class FirehoseAccumulator<I> extends AbstractAccumulator<I, Record> {

    protected FirehoseMapper<I> firehoseMapper;

    public FirehoseAccumulator(double byteSizeLimit, FirehoseMapper<I> firehoseMapper, IAccumulatorLengthFunction<I> accumulatorLengthFunction)
    {
        super(byteSizeLimit);
        this.firehoseMapper = firehoseMapper;
        this.accumulatorLengthFunction = accumulatorLengthFunction;
    }

    @Override
    public List<Record> getRecords() {

        IFirehoseStringMapper<I> stringMapper = firehoseMapper.getStringMapper();
        IFirehoseByteMapper byteMapper = firehoseMapper.getByteMapper();
        IFirehoseRecordMapper<I> recordMapper = firehoseMapper.getRecordMapper();

        return recordMapper.apply(stringMapper, byteMapper)
                .apply(accumulator);
    }

    @Override
    public IAccumulatorLengthFunction<I> accumulatorLengthFunction() {
        return accumulatorLengthFunction;
    }
}
