package it.enryold.quasarflow.models;

import it.enryold.quasarflow.interfaces.IFlowable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QHiearchy {

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


    public void print(String prefix, boolean isTail) {


        System.out.println(prefix + (isTail ? "└── " : "├── ") + this.getFlowable().getName());
        for (int i = 0; i < nestedFlowables.size() - 1; i++) {
            nestedFlowables.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (nestedFlowables.size() > 0) {
            nestedFlowables.get(nestedFlowables.size() - 1)
                    .print(prefix + (isTail ?"    " : "│   "), true);
        }
    }

    public void print(){
        print("", false);
    }

}
