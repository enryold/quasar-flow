package it.enryold.quasarflow;


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

public class ProcessorTests extends TestUtils {



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


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addFlow(emitter -> new QProcessor<>(emitter).process(String::length))
                .addConsumer()
                .consume(resultQueue::put)
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


    @Test
    public void testSingleProcessor() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .process()
                .addConsumer()
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


    @Test
    public void testSingleProcessorWithTransformFunction() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<Integer> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .process(String::length)
                .addConsumer()
                .consume(resultQueue::put)
                .start();

        try {
            List<Integer> results = getResults(resultQueue, elements, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results.size(), elements, "Elements are:" + results.size() + " expected " + elements);
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }
    }


    @Test
    public void testMultiProcessorFanIn() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanIn(2)
                .addConsumer()
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


    @Test
    public void testMultiProcessorFanInWithTransformFunction() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<Integer> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanIn(2, String::length)
                .addConsumer()
                .consume(resultQueue::put)
                .start();


        try {
            List<Integer> results = getResults(resultQueue, elements, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results.size(), elements, "Elements are:" + results.size() + " expected " + elements);
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }
    }



    @Test
    public void testSizeBatchingFanIn_CASE_FLUSH_AFTER_TIME_PERIOD_MULTI_WORKER() {

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



        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanInAndSizeBatching(workers, batchSize, flushSeconds, timeUnit)
                .addConsumer()
                .consume(resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, 2, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(results.size(), 2, "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }



    }



    @Test
    public void testSizeBatchingFanIn_CASE_FLUSH_BEFORE_TIME_PERIOD_MULTI_WORKER() {

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




        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanInAndSizeBatching(workers, batchSize, flushSeconds, timeUnit)
                .addConsumer()
                .consume(resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, 2, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(results.size(), 2, "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }


    }





    @Test
    public void testByteBatchingFanIn_CASE_FLUSH_AFTER_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int TEN_BYTE_STRING_SIZE = 10;
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


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanInAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit)
                .addConsumer()
                .consume(resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, 2, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(results.size(), 2, "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 10), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 9), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }

    }



    @Test
    public void testByteBatchingFanIn_CASE_FLUSH_BEFORE_TIME_PERIOD_MULTI_WORKER() {

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


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanInAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit)
                .addConsumer()
                .consume(resultQueue::put)
                .start();


        List<List<String>> results;
        try {
            results = getResults(resultQueue, 3, flushSeconds+1, TimeUnit.SECONDS);
            // EXPECT 4 ELEMENTS IN
            assertEquals(3, results.size(), "Elements are:" + results.size() + " expected " + 3);
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 20), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 20), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(List::size).anyMatch(s -> s == 1), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }




    }





    @Test
    public void testMultiProcessorFanOut() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanOut(2)
                .cycle(emitter -> emitter.addConsumer().consume(resultQueue::put))
                .start();


        try {
            List<String> results = getResults(resultQueue, 19, flushSeconds, TimeUnit.SECONDS);
            // EXPECT 4 ELEMENTS IN
            assertEquals(results.size(), elements, "Elements are:" + results.size() + " expected " + elements);

        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }

    }


    @Test
    public void testMultiProcessorFanOutWithTransformFunction() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<Integer> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanOut(2, String::length)
                .cycle(emitter -> emitter.addConsumer().consume(resultQueue::put))
                .start();


        List<Integer> results;
        try {
            results = getResults(resultQueue, 19, flushSeconds, TimeUnit.SECONDS);
            // EXPECT 4 ELEMENTS IN
            assertEquals(results.size(), elements, "Elements are:" + results.size() + " expected " + elements);

        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }


    }



    @Test
    public void testSizeBatchingFanOut_CASE_FLUSH_AFTER_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 39;
        int batchSize = 20;
        int workers = 2;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<FlushedObject<List<String>>> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanOutAndSizeBatching(workers, batchSize, flushSeconds, timeUnit)
                .cycle(emitter -> emitter.addConsumer().consume(elm -> {
                    resultQueue.put(new FlushedObject<>(elm));
                }))
                .start();


        List<FlushedObject<List<String>>> results;
        try {
            results = getResults(resultQueue, 2, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }


    }



    @Test
    public void testSizeBatchingFanOut_CASE_FLUSH_BEFORE_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 21;
        int batchSize = 20;
        int workers = 1;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<FlushedObject<List<String>>> resultQueue = resultQueue();



        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanOutAndSizeBatching(workers, batchSize, flushSeconds, timeUnit)
                .cycle(emitter -> emitter.addConsumer().consume(elm -> {
                    resultQueue.put(new FlushedObject<>(elm));
                }))
                .start();


        List<FlushedObject<List<String>>> results;
        try {
            results = getResults(resultQueue, 2, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == batchSize), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == (elements-batchSize)), "Sublist have different size as expected");

        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }


    }





    @Test
    public void testByteBatchingFanOut_CASE_FLUSH_AFTER_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 19;
        int byteSizeLimit = 200;
        int workers = 2;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = tenByteStringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<FlushedObject<List<String>>> resultQueue = resultQueue();

        // ACCUMULATOR FACTORY
        IAccumulatorFactory<String, String> stringAccumulatorFactory = () -> new StringAccumulator(byteSizeLimit);


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanOutAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit)
                .cycle(emitter -> emitter.addConsumer().consume(elm -> {
                    resultQueue.put(new FlushedObject<>(elm));
                }))
                .start();



        List<FlushedObject<List<String>>> results;
        try {
            results = getResults(resultQueue, 2, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(2, results.size(), "Elements are:" + results.size() + " expected " + 2);
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == 10), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == 9), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }






    }



    @Test
    public void testByteBatchingFanOut_CASE_FLUSH_BEFORE_TIME_PERIOD_MULTI_WORKER() {

        // PARAMS
        int elements = 41;
        int byteSizeLimit = 200;
        int workers = 2;
        int flushSeconds = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // EMITTER
        IEmitterTask<String> stringEmitter = tenByteStringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<FlushedObject<List<String>>> resultQueue = resultQueue();

        // ACCUMULATOR FACTORY
        IAccumulatorFactory<String, String> stringAccumulatorFactory = () -> new StringAccumulator(byteSizeLimit);


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor()
                .processWithFanOutAndByteBatching(workers, stringAccumulatorFactory, flushSeconds, timeUnit)
                .cycle(emitter -> emitter.addConsumer().consume(elm -> {
                    resultQueue.put(new FlushedObject<>(elm));
                }))
                .start();


        List<FlushedObject<List<String>>> results;
        try {
            results = getResults(resultQueue, 3, flushSeconds+1, TimeUnit.SECONDS);
            assertEquals(3, results.size(), "Elements are:" + results.size() + " expected " + 3);
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == 20), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == 20), "Sublist have different size as expected");
            assertTrue(results.stream().mapToInt(s -> s.getObj().size()).anyMatch(s -> s == 1), "Sublist have different size as expected");
        } catch (InterruptedException e) {
            fail();
        }finally {
            //currentFlow.destroy();
        }




    }


}
