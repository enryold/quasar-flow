package it.enryold.quasarflow.abstracts;

import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IEmitterList;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.Injector;

import java.util.List;

public abstract class AbstractEmitterList<T> implements IEmitterList<T> {

    private List<IEmitter<T>> emitters;
    private IFlow flow;


    public AbstractEmitterList(List<IEmitter<T>> emitters){
        for(IEmitter<T> e : emitters){
            if(flow == null){ flow = e.flow(); }
            if(flow != null && flow.hashCode() != e.flow().hashCode()){
                throw new RuntimeException("Cannot build emitter list on different flows!");
            }
        }
        this.emitters = emitters;
    }

    @Override
    public IFlow flow() {
        return flow;
    }

    @Override
    public IFlow cycle(Injector<IEmitter<T>> injector) {
        for(IEmitter<T> em : emitters){ injector.accept(em);}
        return flow;
    }
}
