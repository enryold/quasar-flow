package it.enryold.quasarflow;


import co.paralleluniverse.fibers.SuspendExecution;
import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.models.FlushedObject;
import it.enryold.quasarflow.models.QProcessor;
import it.enryold.quasarflow.models.StringAccumulator;
import it.enryold.quasarflow.models.utils.QSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class QuasarTypedFlowTests extends TestUtils {



    @AfterEach
    public void afterEach(){

        this.printRuntime();
    }


    @Test
    public void testSingleProcessorInjected() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<Integer> resultQueue = resultQueue();



        QuasarTypedFlow<String> typedFlow = QuasarTypedFlow.<String>newFlow(QSettings.test());

        typedFlow
                .getEmitter()
                .map(emitter -> new QProcessor<>(emitter).process(() -> String::length))
                .addConsumer()
                .consume(resultQueue::put)
                .start();

        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consume(elm -> {
                    try {
                        typedFlow.getEmitter().getChannel().send(elm);
                    } catch (SuspendExecution | InterruptedException suspendExecution) {
                        suspendExecution.printStackTrace();
                    }
                })
                .start();






        List<Integer> results = null;
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
