package it.enryold.quasarflow;


import it.enryold.quasarflow.components.IAccumulator;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.QConsumer;
import it.enryold.quasarflow.models.metrics.QMetric;
import it.enryold.quasarflow.models.metrics.QMetricAccumulator;
import it.enryold.quasarflow.models.metrics.QMetricSummary;
import it.enryold.quasarflow.models.utils.QSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MetricEmitterTests extends TestUtils {


    @AfterEach
    public void afterEach(){
        this.printRuntime();


      
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
                .consume(emitter -> new QConsumer<>(emitter)
                        .consumeWithByteBatching(
                                () -> new QMetricAccumulator(1_000_000),
                                50,
                                TimeUnit.MILLISECONDS, elms -> {
                                    new QMetricSummary(elms).printSummary(log::info);
                                }));

        metricEmitter.flow().start();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test(), metricEmitter.getChannel())
                .broadcastEmitter(stringEmitter, "stringEmitter")
                .addConsumer("stringConsumer")
                .consume(resultQueue::put)
                .start();


        List<String> results = null;
        try {
            results = getResults(resultQueue, elements, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results.size(), elements, "Elements are:" + results.size() + " expected " + elements);
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }


    }



}
