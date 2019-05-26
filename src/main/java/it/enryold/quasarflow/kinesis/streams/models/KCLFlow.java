package it.enryold.quasarflow.kinesis.streams.models;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.IFlowable;
import it.enryold.quasarflow.kinesis.streams.consumer.v1.RecordProcessorFactory;

import java.util.ArrayList;
import java.util.List;


public class KCLFlow implements IFlow {

    private List<IFlowable> startables = new ArrayList<>();
    private Worker kclWorker;
    private IRecordProcessorFactory recordProcessorFactory;
    private KinesisClientLibConfiguration kinesisClientLibConfiguration;


    public KCLFlow(
                   KinesisClientLibConfiguration kinesisClientLibConfiguration){
        this.kinesisClientLibConfiguration = kinesisClientLibConfiguration;
    }

    @Override
    public void addStartable(IFlowable startable) {
        startables.add(startable);
    }


    public void setRecordProcessorFactory(RecordProcessorFactory recordProcessorFactory) {
        this.recordProcessorFactory = recordProcessorFactory;
    }

    @Override
    public IFlow start() {


        for(int i = startables.size()-1; i >= 0; i--){
            IFlowable s = startables.get(i);
            System.out.println("FLOW: "+s.toString());
            s.start();
        }

        kclWorker = new Worker.Builder()
                .recordProcessorFactory(recordProcessorFactory)
                .config(kinesisClientLibConfiguration)
                .build();
        kclWorker.run();



        return this;
    }

    @Override
    public void destroy() {
        startables.forEach(IFlowable::destroy);
    }
}
