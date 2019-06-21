package it.enryold.quasarflow;


import com.google.common.collect.Lists;
import it.enryold.quasarflow.chain.FiberChain;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.models.utils.QSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class LoadTests extends TestUtils {


    @AfterEach
    public void afterEach(){
        this.printRuntime();


    }




    @Test
    public void testFiberChain() {

        // PARAMS
        int elements = 100_000;
        int flushSeconds = 5;

        // EMITTER
        List<String> stringList = stringList(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<Integer> resultQueue = resultQueue();


        FiberChain.init("stringChain", stringList)
                .transform("toInt", strings -> strings.stream().map(String::length).collect(Collectors.toList()))
                .transform("toStringAgain", integers -> integers.stream().map(String::valueOf).collect(Collectors.toList()))
                .transform("toIntAgain", strings -> strings.stream().map(String::length).collect(Collectors.toList()))
                .consume("toQueue", resultQueue::addAll);


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
    public void testFiberChainSplitter() {

        // PARAMS
        int elements = 1_000_000;
        int flushSeconds = 5;

        // EMITTER
        List<String> stringList = stringList(elements);

        // OUTPUT CHANNEL
        LinkedTransferQueue<Integer> resultQueue = resultQueue();


        FiberChain.init("stringChain", stringList)
                .transform("toInt", strings -> strings.stream().map(String::length).collect(Collectors.toList()))
                .transform("toStringAgain", integers -> integers.stream().map(String::valueOf).collect(Collectors.toList()))
                .transform("toIntAgain", strings -> strings.stream().map(String::length).collect(Collectors.toList()))
                .consume("toQueue", resultQueue::addAll, s -> Lists.partition(s, 100_000));


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
