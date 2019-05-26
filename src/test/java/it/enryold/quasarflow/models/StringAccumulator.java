package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractAccumulator;
import it.enryold.quasarflow.components.IAccumulatorLengthFunction;

import java.util.List;

public class StringAccumulator extends AbstractAccumulator<String, String> {
    public StringAccumulator(double byteSizeLimit) {
        super(byteSizeLimit);
    }


    @Override
    public List<String> getRecords() {
        return accumulator;
    }

    @Override
    public IAccumulatorLengthFunction<String> accumulatorLengthFunction() {
        return s -> (double)s.length();
    }
}
