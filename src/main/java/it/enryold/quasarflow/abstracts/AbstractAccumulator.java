package it.enryold.quasarflow.abstracts;

import it.enryold.quasarflow.components.IAccumulator;
import it.enryold.quasarflow.components.IAccumulatorLengthFunction;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAccumulator<E, T> implements IAccumulator<E, T> {

    private double byteSizeLimit;
    protected List<E> accumulator = new ArrayList<>();
    private AtomicDouble accumulatorSize = new AtomicDouble(0);
    protected IAccumulatorLengthFunction<E> accumulatorLengthFunction;

    public AbstractAccumulator(double byteSizeLimit){
        this.byteSizeLimit = byteSizeLimit;
        this.accumulatorLengthFunction = accumulatorLengthFunction();
    }


    @Override
    public boolean add(E obj) {

        double size = objectSize(obj);

        if(shouldBecomeFull(size)){
            return false;
        }

        accumulator.add(obj);
        accumulatorSize.addAndGet(size);
        return true;
    }


    private double objectSize(E obj)
    {
        return accumulatorLengthFunction.apply(obj);
    }

    private boolean shouldBecomeFull(double size){
        return (accumulatorSize.get() + size > byteSizeLimit);
    }
}
