package it.enryold.quasarflow.kinesis.firehose.accumulators.interfaces;

import com.amazonaws.services.kinesisfirehose.model.Record;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface IFirehoseRecordMapper<I> extends BiFunction<IFirehoseStringMapper<I>, IFirehoseByteMapper, Function<List<I>, List<Record>>> {
}
