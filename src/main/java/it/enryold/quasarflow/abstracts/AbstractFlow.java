package it.enryold.quasarflow.abstracts;

import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.IFlowable;
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
    private Channel<QMetric> metricChannel;

    public AbstractFlow(){
        this(null, QSettings.highLoad(), null);
    }

    public AbstractFlow(QSettings settings){
        this(null, settings, null);
    }

    public AbstractFlow(QSettings settings, Channel<QMetric> metricChannel){
        this(null, settings, metricChannel);
    }

    public AbstractFlow(String name, QSettings settings, Channel<QMetric> metricChannel){
        this.settings = settings;
        this.name = name == null ? getClass().getSimpleName()+this.hashCode() : name;
        this.metricChannel = metricChannel;
    }


    @Override
    public String getName() {
        return name;
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
    public <I extends IFlow> I start() {

        for(int i = startables.size()-1; i >= 0; i--){
            IFlowable s = startables.get(i);
            Optional.ofNullable(metricChannel).ifPresent(s::withMetricChannel);
            s.start();
        }

        return (I)this;
    }

    @Override
    public void destroy() {
        startables.forEach(IFlowable::destroy);
    }

}
