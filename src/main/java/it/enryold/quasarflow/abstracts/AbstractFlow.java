package it.enryold.quasarflow.abstracts;

import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.IFlowable;
import it.enryold.quasarflow.interfaces.IProcessor;
import it.enryold.quasarflow.models.QHiearchy;
import it.enryold.quasarflow.models.metrics.QMetric;
import it.enryold.quasarflow.models.utils.QSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractFlow implements IFlow {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private List<IFlowable> startables = new ArrayList<>();
    private QSettings settings;
    private String name;
    private QHiearchy hiearchy;
    private List<IFlowable> nestedList = new ArrayList<>();
    private IFlowable currentNested;

    public AbstractFlow(){
        this(null, QSettings.highLoad());
    }

    public AbstractFlow(QSettings settings){
        this(null, settings);
    }

    public AbstractFlow(String name, QSettings settings){
        this.settings = settings;
        this.name = name == null ? getClass().getSimpleName()+this.hashCode() : name;
    }


    @Override
    public String getName() {
        return name;
    }

    public void setParentNested()
    {
        resetNested();
    }

    @Override
    public void setNested(IFlowable nested) {

        int requestNestedDeep = hiearchy.find(nested).map(QHiearchy::level).orElse(0);
        int lastNestedDeep = hiearchy.find(currentNested).map(QHiearchy::level).orElse(0);

        if(currentNested != null && !currentNested.equals(nested)){

            if(requestNestedDeep > lastNestedDeep)
            {
                addNested(nested);
            }else{
                resetNested();
            }

        }else{
            addNested(nested);
        }

    }


    private void addNested(IFlowable startable){
        nestedList.add(startable);
        currentNested = startable;
    }

    private void resetNested()
    {
        nestedList.remove(nestedList.size()-1);

        if(nestedList.size() == 0){
            addNested(hiearchy.getFlowable());
        }else{
            currentNested = nestedList.get(nestedList.size()-1);
        }

    }


    @Override
    public void addStartable(IFlowable startable) {

        startables.add(startable);

        if(hiearchy == null){
            hiearchy = new QHiearchy(startable);
            addNested(startable);
        }else{
            hiearchy.find(currentNested).ifPresent(s -> s.addNestedFlowables(startable));
        }
    }

    @Override
    public QSettings getSettings() {
        return settings;
    }

    @Override
    public <I extends IFlow> I start() {

        for(int i = startables.size()-1; i >= 0; i--){
            IFlowable s = startables.get(i);
            s.start();
        }

        return (I)this;
    }

    @Override
    public void destroy() {
        startables.forEach(IFlowable::destroy);
    }


    public void print(){
        hiearchy.print();
    }

    public void printMetrics(){
        hiearchy.printMetrics();
    }



}
