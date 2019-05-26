package it.enryold.quasarflow.models;

import it.enryold.quasarflow.abstracts.AbstractEmitterList;
import it.enryold.quasarflow.interfaces.IEmitter;

import java.util.List;

public class QEmitterList<I> extends AbstractEmitterList<I> {
    public QEmitterList(List<IEmitter<I>> iEmitters) {
        super(iEmitters);
    }
}
