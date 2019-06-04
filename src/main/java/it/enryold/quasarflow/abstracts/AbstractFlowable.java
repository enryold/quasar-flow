package it.enryold.quasarflow.abstracts;

import it.enryold.quasarflow.interfaces.IFlowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFlowable<T> implements IFlowable<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected String name;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    protected void log(String message){
        log.debug(this.name+" "+message);
    }

    protected void error(String message){
        log.error(this.name+" "+message);
    }
}
