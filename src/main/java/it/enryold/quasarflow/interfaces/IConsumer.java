package it.enryold.quasarflow.interfaces;

import it.enryold.quasarflow.components.IAccumulatorFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface IConsumer<T> extends IFlowable<T>  {

    IFlow consume(IConsumerTask<T> task);

    IFlow consumeWithSizeBatching(int chunkSize,
                                  int flushTimeout,
                                  TimeUnit flushTimeUnit,
                                  IConsumerTask<List<T>> task);

    <O>IFlow consumeWithByteBatching(IAccumulatorFactory<T, O> accumulatorFactory,
                                     int flushTimeout,
                                     TimeUnit flushTimeUnit,
                                     IConsumerTask<List<O>> task);

    IFlow consumeWithFanOutAndSizeBatching(int workers,
                                           int chunkSize,
                                           int flushTimeout,
                                           TimeUnit flushTimeUnit,
                                           IConsumerTaskFactory<List<T>> task);

    <O>IFlow consumeWithFanOutAndByteBatching(int workers,
                                              IAccumulatorFactory<T, O> accumulatorFactory,
                                              int flushTimeout,
                                              TimeUnit flushTimeUnit,
                                              IConsumerTaskFactory<List<O>> task);
}
