package it.enryold.quasarflow.models;

import it.enryold.quasarflow.interfaces.IFlowable;
import it.enryold.quasarflow.models.metrics.QMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QHiearchy {

    protected final Logger log = LoggerFactory.getLogger(getClass());


    private int index = 0;
    private IFlowable flowable;
    private List<QHiearchy> nestedFlowables = new ArrayList<>();

    public QHiearchy(IFlowable flowable){
        this.flowable = flowable;
        this.index = 0;
    }

    private QHiearchy(IFlowable flowable, int idx){
        this.flowable = flowable;
        this.index = idx;
    }

    public IFlowable getFlowable() {
        return flowable;
    }


    public void addNestedFlowables(IFlowable IFlowable) {
        this.nestedFlowables.add(new QHiearchy(IFlowable, index+1));
    }

    public int level(){
        return index;
    }

    @Override
    public int hashCode() {
        return ("h"+flowable).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    public Optional<QHiearchy> find(IFlowable flowable){

        if(this.flowable.equals(flowable)){
            return Optional.of(this);
        }else{

            int idx = this.nestedFlowables.lastIndexOf(new QHiearchy(flowable));
            if(idx != -1){ return Optional.of(this.nestedFlowables.get(idx)); }

            for(QHiearchy q : this.nestedFlowables){
                Optional<QHiearchy> opt = q.find(flowable);
                if(opt.isPresent()){
                    return opt;
                }
            }

            return Optional.empty();

        }
    }


    protected void print(String prefix, boolean isTail) {


        log.info(prefix + (isTail ? "└── " : "├── ") + this.getFlowable().getName());
        for (int i = 0; i < nestedFlowables.size() - 1; i++) {
            nestedFlowables.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (nestedFlowables.size() > 0) {
            nestedFlowables.get(nestedFlowables.size() - 1)
                    .print(prefix + (isTail ?"    " : "│   "), true);
        }
    }

    protected void printMetrics(String prefix, int spaceChars, boolean isTail) {



        String metricString= this.flowable.getMetrics().stream().map(QMetric::toString).collect(Collectors.joining(" | "));
        String logString = prefix + (isTail ? "└── " : "├── ") + this.getFlowable().getName();
        String spaceString = " ";

        int spaces = spaceChars - logString.length();

        for(int i = 0; i < spaces; i++){
            metricString = spaceString+metricString;
        }


        log.info(logString+metricString);


        for (int i = 0; i < nestedFlowables.size() - 1; i++) {
            nestedFlowables.get(i).printMetrics(prefix + (isTail ? "    " : "│   "), spaceChars,false);
        }
        if (nestedFlowables.size() > 0) {
            nestedFlowables.get(nestedFlowables.size() - 1)
                    .printMetrics(prefix + (isTail ?"    " : "│   "), spaceChars,true);
        }
    }

    public void print(String name){
        print(name, false);
    }

    public void printMetrics(String name,  int spaceChars){
        printMetrics(name, spaceChars, false);
    }

}
