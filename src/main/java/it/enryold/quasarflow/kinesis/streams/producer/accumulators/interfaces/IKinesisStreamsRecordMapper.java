package it.enryold.quasarflow.kinesis.streams.producer.accumulators.interfaces;


import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface IKinesisStreamsRecordMapper<I> extends BiFunction<IKinesisStreamsStringMapper<I>, IKinesisStreamsByteMapper, Function<List<I>, List<ByteBuffer>>> {
}
