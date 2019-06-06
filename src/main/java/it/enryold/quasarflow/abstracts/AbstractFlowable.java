package it.enryold.quasarflow.abstracts;

import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.IFlowable;
import it.enryold.quasarflow.models.utils.QSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractFlowable implements IFlowable {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected String name;
    protected IFlow flow;
    protected QSettings settings;
    protected AtomicLong producedElements = new AtomicLong(0L);
    protected AtomicLong receivedElements = new AtomicLong(0L);


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
