package it.enryold.quasarflow.interfaces;

import it.enryold.quasarflow.components.IAccumulatorFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface IProcessor<I> extends IFlowable<I> {




    <EM extends IEmitter<I>> EM process();


    <T, EM extends IEmitter<T>> EM process(ITransform<I, T> transform);


    IEmitterList<I> processWithFanOut(int workers);


    <T> IEmitterList<T> processWithFanOut(int workers, ITransform<I, T> transform);

    IEmitterList<List<I>> processWithFanOutAndSizeBatching(
            int workers,
            int chunkSize,
            int flushTimeout,
            TimeUnit flushTimeUnit);

    <T> IEmitterList<List<T>> processWithFanOutAndByteBatching(
            int workers,
            IAccumulatorFactory<I, T> accumulatorFactory,
            int flushTimeout,
            TimeUnit flushTimeUnit);


    <EM extends IEmitter<I>> EM processWithFanIn(int workers);


    <T, EM extends IEmitter<T>> EM processWithFanIn(int workers,
                                                    ITransform<I, T> transform);

    <EM extends IEmitter<List<I>>> EM processWithFanInAndSizeBatching(
            int workers,
            int chunkSize,
            int flushTimeout,
            TimeUnit flushTimeUnit);

    <T, EM extends IEmitter<List<T>>> EM processWithFanInAndByteBatching(
            int workers,
            IAccumulatorFactory<I, T> accumulatorFactory,
            int flushTimeout,
            TimeUnit flushTimeUnit);




    void destroy();
}

