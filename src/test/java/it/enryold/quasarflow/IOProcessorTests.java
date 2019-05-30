package it.enryold.quasarflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.io.http.HTTPProcessor;
import it.enryold.quasarflow.io.http.models.QHTTPRequest;
import it.enryold.quasarflow.io.http.models.QHTTPResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOProcessorTests extends TestUtils {


    @AfterEach
    public void afterEach(){

        this.printRuntime();


    }


    @Test
    public void test() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 10;
        int fibers = 4;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<QHTTPRequest<String>> requestTask = publisherChannel -> {
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

                    QHTTPRequest<String> qhttpRequest = new QHTTPRequest<>(requestPrefix+i, request, dataPrefix+i);

                    publisherChannel.send(qhttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<QHTTPResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        IFlow currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .ioProcessor(emitter -> new HTTPProcessor<>(emitter))
                .processWithFanIn(fibers)
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<QHTTPResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("Requests summed execution time: "+results.stream().mapToLong(QHTTPResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

        for(QHTTPResponse<String> resp : results){
            String idx0 = resp.getAttachedDatas().split(dataPrefix)[1];
            String idx1 = resp.getRequestId().split(requestPrefix)[1];
            assertEquals(idx0, idx1);
        }

        assertEquals(results.size(), requests);

        //currentFlow.destroy();

    }
}
