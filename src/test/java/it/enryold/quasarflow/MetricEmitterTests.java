package it.enryold.quasarflow;


import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QEmitter;
import it.enryold.quasarflow.models.StringAccumulator;
import it.enryold.quasarflow.models.utils.QMetric;
import it.enryold.quasarflow.models.utils.QSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MetricEmitterTests extends TestUtils {

    private IFlow currentFlow;

    @AfterEach
    public void afterEach(){
        this.printRuntime();


        if(currentFlow != null){
            currentFlow.destroy();
        }
    }




    @Test
    public void testMetric() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueue = resultQueue();


        IEmitter<QMetric> metricEmitter = MetricFlow.newFlow()
                .metricEmitter()
                .addConsumer(c -> c.consumeWithSizeBatching(
                        100,
                        50,
                        TimeUnit.MILLISECONDS,
                        elm -> {
                            elm.stream().collect(Collectors.groupingBy(QMetric::getComponentName, Collectors.counting()))
                                    .forEach((k,v) -> System.out.println("Received "+k+" "+v+" times"));
                        }));

        metricEmitter.flow().start();

        currentFlow = QuasarFlow.newFlow(QSettings.test(), metricEmitter.getChannel())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consume(resultQueue::put)
                .start();


        List<String> results = null;
        try {
            results = getResults(resultQueue, elements, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results.size(), elements, "Elements are:" + results.size() + " expected " + elements);
        } catch (InterruptedException e) {
            fail();
        }


    }



}
