package it.enryold.quasarflow.kinesis.streams.producer.accumulators.generic;

import co.paralleluniverse.fibers.Suspendable;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsByteMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsRecordMapper;
import it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces.IKinesisStreamsStringMapper;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BasicKinesisStreamsRecordMapper<I> implements IKinesisStreamsRecordMapper<I> {


    public static final double RECORD_MAX_SIZE_IN_BYTES = 999_999.0;


    private ByteBuffer buildRecord(byte[] raw)
    {
        return ByteBuffer.wrap(raw);
    }

    @Override
    @Suspendable
    public Function<List<I>, List<ByteBuffer>> apply(IKinesisStreamsStringMapper<I> iiKinesisStreamsStringMapper, IKinesisStreamsByteMapper iKinesisStreamsByteMapper) {


        return is -> {

            if(is == null) { return new ArrayList<>(); }
            if(is.size() == 0) { return new ArrayList<>(); }


            Optional<byte[]> raw = iKinesisStreamsByteMapper.apply(iiKinesisStreamsStringMapper.apply(is));

            if(!raw.isPresent())
            {
                return new ArrayList<>();
            }


            int splitter = (int) Math.ceil(raw.get().length / RECORD_MAX_SIZE_IN_BYTES);

            return Lists.partition(is, is.size() / splitter).stream()
                    .map(l -> iKinesisStreamsByteMapper.apply(iiKinesisStreamsStringMapper.apply(l)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(this::buildRecord)
                    .collect(Collectors.toList());
        };
    }
}
