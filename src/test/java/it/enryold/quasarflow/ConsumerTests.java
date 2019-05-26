package it.enryold.quasarflow;


import it.enryold.quasarflow.components.IAccumulatorFactory;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.models.StringAccumulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsumerTests extends TestUtils {

    private IFlow currentFlow;

    @BeforeEach
    public void beforeEach(){
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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consume(resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);

        while (resultDeadline > System.currentTimeMillis()){
        }

        List<String> results = getResults(resultQueue);

        assertEquals(results.size(), elements, "Elements are:" + results.size() + " expected " + elements);

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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithSizeBatching(batchSize, flushSeconds, timeUnit, resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);

        while (resultDeadline > System.currentTimeMillis()){
        }


        List<List<String>> results = getResults(resultQueue);

        assertEquals(1, results.size(), "Elements are:" + results.size() + " expected " + 1);

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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithSizeBatching(batchSize, flushSeconds, timeUnit, resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);

        

        while (resultDeadline > System.currentTimeMillis()){
        }


        List<List<String>> results = getResults(resultQueue);

        assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndSizeBatching(workers, batchSize, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);





        while (resultDeadline > System.currentTimeMillis()){
        }


        List<List<String>> results = getResults(resultQueue);


        assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndSizeBatching(workers, batchSize, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);





        while (resultDeadline > System.currentTimeMillis()){
        }


        List<List<String>> results = getResults(resultQueue);

        // EXPECT 4 ELEMENTS IN
        assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithByteBatching(stringAccumulatorFactory, flushSeconds, timeUnit, resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);



        while (resultDeadline > System.currentTimeMillis()){
        }


        List<List<String>> results = getResults(resultQueue);

        assertEquals(1, results.size(), "Elements are:" + results.size() + " expected " + 1);

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

        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithByteBatching(stringAccumulatorFactory, flushSeconds, timeUnit, resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);


        while (resultDeadline > System.currentTimeMillis()){
        }


        List<List<String>> results = getResults(resultQueue);

        assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (byteSizeLimit/TEN_BYTE_STRING_SIZE)), "Sublist have different size as expected");
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-(byteSizeLimit/TEN_BYTE_STRING_SIZE))), "Sublist have different size as expected");

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

        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);



        while (resultDeadline > System.currentTimeMillis()){
        }


        List<List<String>> results = getResults(resultQueue);

        assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 10), "Sublist have different size as expected");
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 9), "Sublist have different size as expected");
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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consumeWithFanOutAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit, () -> resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);





        while (resultDeadline > System.currentTimeMillis()){
        }



        List<List<String>> results = getResults(resultQueue);

        // EXPECT 4 ELEMENTS IN
        assertEquals(3, results.size(), "Elements are:" + results.size() + " expected " + 3);
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 20), "Sublist have different size as expected");
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 20), "Sublist have different size as expected");
        assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 1), "Sublist have different size as expected");

    }


}
