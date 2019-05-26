package it.enryold.quasarflow.kinesis.firehose.accumulators.generic;

import com.amazonaws.services.kinesisfirehose.model.Record;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseByteMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseRecordMapper;
import it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces.IFirehoseStringMapper;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BasicFirehoseRecordMapper<I> implements IFirehoseRecordMapper<I> {


    public static final double RECORD_MAX_SIZE_IN_BYTES = 999_999.0;


    private Record buildRecord(byte[] raw)
    {
        Record record=new Record();
        record.setData(ByteBuffer.wrap(raw));
        return record;
    }

    @Override
    public Function<List<I>, List<Record>> apply(IFirehoseStringMapper<I> iiFirehoseStringMapper, IFirehoseByteMapper iFirehoseByteMapper) {


        return is -> {

            if(is == null) { return new ArrayList<>(); }
            if(is.size() == 0) { return new ArrayList<>(); }


            Optional<byte[]> raw = iFirehoseByteMapper.apply(iiFirehoseStringMapper.apply(is));

            if(!raw.isPresent())
            {
                return new ArrayList<>();
            }


            int splitter = (int) Math.ceil(raw.get().length / RECORD_MAX_SIZE_IN_BYTES);

            return Lists.partition(is, is.size()/splitter).stream()
                    .map(l -> iFirehoseByteMapper.apply(iiFirehoseStringMapper.apply(l)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(this::buildRecord)
                    .collect(Collectors.toList());
        };
    }
}
