package it.enryold.quasarflow;


import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedTransferQueue;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmitterTests extends TestUtils {

    private IFlow currentFlow;

    @BeforeEach
    public void beforeEach(){
        if(currentFlow != null){
            currentFlow.destroy();
        }
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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addConsumer()
                .consume(resultQueue::put)
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);

        while (resultDeadline > System.currentTimeMillis()){
        }

        List<String> results = getResults(resultQueue);

        assertTrue(results.size() == elements, "Elements are:"+results.size()+" expected "+elements);

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


        currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(stringEmitter)
                .addProcessor(p -> p.process().addConsumer(c -> c.consume(resultQueue1::put)))
                .addProcessor(p -> p.process().addConsumer(c -> c.consume(resultQueue2::put)))
                .flow()
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);

        while (resultDeadline > System.currentTimeMillis()){
        }

        List<String> results1 = getResults(resultQueue1);
        List<String> results2 = getResults(resultQueue2);

        assertTrue(results1.size() == elements, "Elements are:"+results1.size()+" expected "+elements);
        assertTrue(results2.size() == elements, "Elements are:"+results2.size()+" expected "+elements);

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


        currentFlow = QuasarFlow.newFlow()
                // TAKE AS ROUTING KEY THE FIRST CHAR IN A STRING
                .routedEmitter(stringEmitter, o -> Optional.of(o.substring(0, 1)))
                .addProcessor("A", p -> p.process().addConsumer(c -> c.consume(resultQueueA::put)))
                .addProcessor("B", p -> p.process().addConsumer(c -> c.consume(resultQueueB::put)))
                .flow()
                .start();

        long resultDeadline = System.currentTimeMillis()+((flushSeconds+1)*1000);

        while (resultDeadline > System.currentTimeMillis()){
        }

        List<String> resultsA = getResults(resultQueueA);
        List<String> resultsB = getResults(resultQueueB);

        assertTrue(resultsA.size() == 2, "Elements are:"+resultsA.size()+" expected "+2);
        assertTrue(resultsB.size() == 2, "Elements are:"+resultsB.size()+" expected "+2);

    }




}
