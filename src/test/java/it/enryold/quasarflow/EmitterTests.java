package it.enryold.quasarflow;


import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.interfaces.IRoutingKeyExtractor;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import it.enryold.quasarflow.models.utils.QSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class EmitterTests extends TestUtils {


    @AfterEach
    public void afterEach(){
        this.printRuntime();
    }


    @Test
    public void testSingleEmitter() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueue = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
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
        }finally {
            currentFlow.destroy();
        }


    }


    @Test
    public void testBroadcastEmitter() {

        // PARAMS
        int elements = 19;
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueue1 = resultQueue();
        LinkedTransferQueue<String> resultQueue2 = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter)
                .addProcessor(p -> p.process().addConsumer(c -> c.consume(resultQueue1::put)))
                .addProcessor(p -> p.process().addConsumer(c -> c.consume(resultQueue2::put)))
                .flow()
                .start();


        try {
            List<String> results1 = getResults(resultQueue1, elements, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results1.size(), elements, "Elements are:" + results1.size() + " expected " + elements);
        } catch (InterruptedException e) {
            fail();
        }finally {
            currentFlow.destroy();
        }

        try {
            List<String> results2 = getResults(resultQueue2, elements, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results2.size(), elements, "Elements are:" + results2.size() + " expected " + elements);
        } catch (InterruptedException e) {
            fail();
        }finally {
            currentFlow.destroy();
        }




    }



    @Test
    public void testRoutedEmitter() {

        // PARAMS
        int flushSeconds = 1;

        // EMITTER
        IEmitterTask<String> stringEmitter = new IEmitterTask<String>() {

            String[] elementsToProduce = {"A1", "A2", "B1", "B2", "C0"};

            @Override
            public void emit(Channel<String> publisherChannel) throws InterruptedException, SuspendExecution {
                for(String str : elementsToProduce){ publisherChannel.send(str);}
            }
        };

        // OUTPUT CHANNEL
        LinkedTransferQueue<String> resultQueueA = resultQueue();
        LinkedTransferQueue<String> resultQueueB = resultQueue();


        IFlow currentFlow = QuasarFlow.newFlow(QSettings.test())
                // TAKE AS ROUTING KEY THE FIRST CHAR IN A STRING
                .routedEmitter(stringEmitter, o -> QRoutingKey.withKey(o.substring(0, 1)))
                .addProcessor(QRoutingKey.withKey("A"), p -> p.process().addConsumer(c -> c.consume(resultQueueA::put)))
                .addProcessor(QRoutingKey.withKey("B"), p -> p.process().addConsumer(c -> c.consume(resultQueueB::put)))
                .flow()
                .start();



        try {
            List<String> results1 = getResults(resultQueueA, 2, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results1.size(), 2, "Elements are:" + results1.size() + " expected " + 2);
        } catch (InterruptedException e) {
            fail();
        }finally {
            currentFlow.destroy();
        }

        try {
            List<String> results2 = getResults(resultQueueB, 2, flushSeconds, TimeUnit.SECONDS);
            assertEquals(results2.size(), 2, "Elements are:" + results2.size() + " expected " + 2);
        } catch (InterruptedException e) {
            fail();
        }finally {
            currentFlow.destroy();
        }


    }




}
