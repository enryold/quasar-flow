package it.enryold.quasarflow.models;

import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.IFlowable;

import java.util.ArrayList;
import java.util.List;


public class QFlow implements IFlow {

    private List<IFlowable> startables = new ArrayList<>();
    private QSettings settings;

    public QFlow(){
        this.settings = QSettings.standard();
    }

    public QFlow(QSettings settings){
        this.settings = settings;
    }


    @Override
    public void addStartable(IFlowable startable) {
        startables.add(startable);
    }

    @Override
    public QSettings getSettings() {
        return settings;
    }

    @Override
    public IFlow start() {

        for(int i = startables.size()-1; i >= 0; i--){
            IFlowable s = startables.get(i);
            System.out.println("FLOW: "+s.toString());
            s.start();
        }

        return this;
    }

    @Override
    public void destroy() {
        startables.forEach(IFlowable::destroy);
    }
}
