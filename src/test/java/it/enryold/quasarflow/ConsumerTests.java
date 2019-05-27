package it.enryold.quasarflow;


import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.models.QSettings;
import it.enryold.quasarflow.models.StringAccumulator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumerTests extends TestUtils {

    private IFlow currentFlow;

    @AfterEach
    public void afterEach(){
        this.printRuntime();


        if(currentFlow != null){
            currentFlow.destroy();
        }
    }



    @Test
    public void testSingleConsumer() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueue = resultQueue();


        currentFlow = QuasarFlow.newFlow(QSettings.test())
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


    @Test
    public void testSizeBatching_CASE_FLUSH_AFTER_TIME_PERIOD() {

        // PARAMS
        int elements = 19;
        int batchSize = 20;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithSizeBatching(batchSize, flushSeconds, timeUnit, resultQueue::put)
                .start();




        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(1, results.size(), "Elements are:" + results.size() + " expected " + 1);
        } catch (InterruptedException e) {
            fail();
        }


    }



    @Test
    public void testSizeBatching_CASE_FLUSH_BEFORE_TIME_PERIOD() {

        // PARAMS
        int elements = 21;
        int batchSize = 20;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithSizeBatching(batchSize, flushSeconds, timeUnit, resultQueue::put)
                .start();

        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

        } catch (InterruptedException e) {
            fail();
        }



    }


    @Test
    public void testSizeBatching_CASE_FLUSH_AFTER_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 39;
        int batchSize = 20;
        int workers = 2;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndSizeBatching(workers, batchSize, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

        } catch (InterruptedException e) {
            fail();
        }



    }



    @Test
    public void testSizeBatching_CASE_FLUSH_BEFORE_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 21;
        int batchSize = 20;
        int workers = 1;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndSizeBatching(workers, batchSize, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            // EXPECT 4 ELEMENTS IN
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

        } catch (InterruptedException e) {
            fail();
        }


    }



    @Test
    public void testByteBatching_CASE_FLUSH_AFTER_TIME_PERIOD() {

        // PARAMS
        int elements = 19;
        int byteSizeLimit = 200;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER (10 byte strings each)
        IEmitterTask<String> stringEmitter = tenByteStringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();

        // ACCUMULATOR FACTORY
        IAccumulatorFactory<String, String> stringAccumulatorFactory = () -> new StringAccumulator(byteSizeLimit);


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithByteBatching(stringAccumulatorFactory, flushSeconds, timeUnit, resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(1, results.size(), "Elements are:" + results.size() + " expected " + 1);
        } catch (InterruptedException e) {
            fail();
        }


    }



    @Test
    public void testByteBatching_CASE_FLUSH_BEFORE_TIME_PERIOD() {

        // PARAMS
        int TEN_BYTE_STRING_SIZE = 10;
        int elements = 21;
        int byteSizeLimit = 200;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = tenByteStringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();

        // ACCUMULATOR FACTORY
        IAccumulatorFactory<String, String> stringAccumulatorFactory = () -> new StringAccumulator(byteSizeLimit);

        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithByteBatching(stringAccumulatorFactory, flushSeconds, timeUnit, resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (byteSizeLimit/TEN_BYTE_STRING_SIZE)), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-(byteSizeLimit/TEN_BYTE_STRING_SIZE))), "Sublist have different size as expected");

        } catch (InterruptedException e) {
            fail();
        }


    }


    @Test
    public void testByteBatching_CASE_FLUSH_AFTER_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 19;
        int byteSizeLimit = 200;
        int workers = 2;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = tenByteStringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();

        // ACCUMULATOR FACTORY
        IAccumulatorFactory<String, String> stringAccumulatorFactory = () -> new StringAccumulator(byteSizeLimit);

        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 10), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 9), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }


    }



    @Test
    public void testByteBatching_CASE_FLUSH_BEFORE_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 41;
        int byteSizeLimit = 200;
        int workers = 2;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = tenByteStringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<List<String>> resultQueue = resultQueue();

        // ACCUMULATOR FACTORY
        IAccumulatorFactory<String, String> stringAccumulatorFactory = () -> new StringAccumulator(byteSizeLimit);


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, elements, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(3, results.size(), "Elements are:" + results.size() + " expected " + 3);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 20), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 20), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 1), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }


    }


}
