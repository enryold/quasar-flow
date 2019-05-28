package it.enryold.quasarflow;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import it.enryold.quasarflow.interfaces.IConsumerTask;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.io.http.HTTPProcessor;
import it.enryold.quasarflow.io.http.consts.QHttpConsts;
import it.enryold.quasarflow.io.http.models.QHTTPRequest;
import it.enryold.quasarflow.io.http.models.QHTTPResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class IOProcessorTests extends TestUtils {

    private IFlow currentFlow;

    @AfterEach
    public void afterEach(){

        this.printRuntime();


        if(currentFlow != null){
            currentFlow.destroy();
        }
    }


    @Test
    public void test() throws InterruptedException {


        int requests = 10;
        int fibers = 4;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<QHTTPRequest> requestTask = publisherChannel -> {
            for(int i=0; i<requests; i++){

                try {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("name", "pippo"+i);
                    payload.put("job", "pippo"+i);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), new ObjectMapper().writeValueAsBytes(payload));
                    Request request = new Request.Builder()
                            .url(url)
                            .put(body)
                            .build();

                    QHTTPRequest qhttpRequest = new QHTTPRequest("REQ"+i, request);

                    publisherChannel.send(qhttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<QHTTPResponse> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .ioProcessor(HTTPProcessor::new)
                .processWithFanIn(4)
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<QHTTPResponse> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("Requests summed execution time: "+results.stream().mapToLong(QHTTPResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

    }
}
