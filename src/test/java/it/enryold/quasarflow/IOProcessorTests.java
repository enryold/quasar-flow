package it.enryold.quasarflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.interfaces.IFlow;
import it.enryold.quasarflow.io.http.ahc.ApacheHttpProcessor;
import it.enryold.quasarflow.io.http.ahc.models.ApacheHttpRequest;
import it.enryold.quasarflow.io.http.ahc.models.ApacheHttpResponse;
import it.enryold.quasarflow.io.http.okhttp.OkHttpProcessor;
import it.enryold.quasarflow.io.http.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.io.http.okhttp.models.OkHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
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
    public void testOkHttpAsync() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 10;
        int fibers = 4;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<OkHttpRequest<String>> requestTask = publisherChannel -> {
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

                    OkHttpRequest<String> okHttpRequest = new OkHttpRequest<>(requestPrefix+i, request, dataPrefix+i);

                    publisherChannel.send(okHttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<OkHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        IFlow currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .map(emitter -> new OkHttpProcessor<>(emitter, true)
                        .processWithFanIn(fibers))
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<OkHttpResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("ASYNC: Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("ASYNC: Requests summed execution time: "+results.stream().mapToLong(OkHttpResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

        for(OkHttpResponse<String> resp : results){
            String idx0 = resp.getAttachedDatas().split(dataPrefix)[1];
            String idx1 = resp.getRequestId().split(requestPrefix)[1];
            assertEquals(idx0, idx1);
        }

        assertEquals(results.size(), requests);

        //currentFlow.destroy();

    }


    @Test
    public void testOkHttpSync() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 10;
        int fibers = 4;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<OkHttpRequest<String>> requestTask = publisherChannel -> {
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

                    OkHttpRequest<String> okHttpRequest = new OkHttpRequest<>(requestPrefix+i, request, dataPrefix+i);

                    publisherChannel.send(okHttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<OkHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        IFlow currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .map(emitter -> new OkHttpProcessor<>(emitter, false)
                        .processWithFanIn(fibers))
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<OkHttpResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("SYNC: Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("SYNC: Requests summed execution time: "+results.stream().mapToLong(OkHttpResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

        for(OkHttpResponse<String> resp : results){
            String idx0 = resp.getAttachedDatas().split(dataPrefix)[1];
            String idx1 = resp.getRequestId().split(requestPrefix)[1];
            assertEquals(idx0, idx1);
        }

        assertEquals(results.size(), requests);

        //currentFlow.destroy();

    }




    @Test
    public void testOkHttpAsyncLoad() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 50;
        int fibers = 50;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<OkHttpRequest<String>> requestTask = publisherChannel -> {
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

                    OkHttpRequest<String> okHttpRequest = new OkHttpRequest<>(requestPrefix+i, request, dataPrefix+i);

                    publisherChannel.send(okHttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<OkHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        IFlow currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .map(emitter -> new OkHttpProcessor<>(emitter, true)
                        .processWithFanIn(fibers))
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<OkHttpResponse<String>> results = this.getResults(responseQueue, requests, 10, TimeUnit.SECONDS);

        System.out.println("ASYNC: Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("ASYNC: Requests summed execution time: "+results.stream().mapToLong(OkHttpResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

        for(OkHttpResponse<String> resp : results){
            String idx0 = resp.getAttachedDatas().split(dataPrefix)[1];
            String idx1 = resp.getRequestId().split(requestPrefix)[1];
            assertEquals(idx0, idx1);
        }

        assertEquals(results.size(), requests);

        //currentFlow.destroy();

    }


    @Test
    public void testOkHttpSyncLoad() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 50;
        int fibers = 50;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<OkHttpRequest<String>> requestTask = publisherChannel -> {
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

                    OkHttpRequest<String> okHttpRequest = new OkHttpRequest<>(requestPrefix+i, request, dataPrefix+i);

                    publisherChannel.send(okHttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<OkHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        IFlow currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .map(emitter -> new OkHttpProcessor<>(emitter, false)
                        .processWithFanIn(fibers))
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<OkHttpResponse<String>> results = this.getResults(responseQueue, requests, 10, TimeUnit.SECONDS);

        System.out.println("SYNC: Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("SYNC: Requests summed execution time: "+results.stream().mapToLong(OkHttpResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

        for(OkHttpResponse<String> resp : results){
            String idx0 = resp.getAttachedDatas().split(dataPrefix)[1];
            String idx1 = resp.getRequestId().split(requestPrefix)[1];
            assertEquals(idx0, idx1);
        }

        assertEquals(results.size(), requests);

        //currentFlow.destroy();

    }


    @Test
    public void testAHC() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 10;
        int fibers = 4;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<ApacheHttpRequest<String>> requestTask = publisherChannel -> {
            for(int i=0; i<requests; i++){

                try {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("name", "pippo"+i);
                    payload.put("job", "pippo"+i);

                    byte[] bytePayload = new ObjectMapper().writeValueAsBytes(payload);

                    BasicHttpEntity httpEntity = new BasicHttpEntity();
                    httpEntity.setContent(new ByteArrayInputStream(bytePayload));
                    httpEntity.setContentLength(bytePayload.length);
                    httpEntity.setContentType("application/json");


                    HttpPut request = new HttpPut();
                    request.setURI(URI.create(url));
                    request.setEntity(httpEntity);

                    ApacheHttpRequest<String> okHttpRequest = new ApacheHttpRequest<>(requestPrefix+i, request, dataPrefix+i);

                    publisherChannel.send(okHttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<ApacheHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        IFlow currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .map(emitter -> new ApacheHttpProcessor<>(emitter)
                        .processWithFanIn(fibers))
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<ApacheHttpResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("Requests summed execution time: "+results.stream().mapToLong(ApacheHttpResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

        for(ApacheHttpResponse<String> resp : results){
            String idx0 = resp.getAttachedDatas().split(dataPrefix)[1];
            String idx1 = resp.getRequestId().split(requestPrefix)[1];
            assertEquals(idx0, idx1);
        }

        assertEquals(results.size(), requests);

        //currentFlow.destroy();

    }



    @Test
    public void testAHCLoad() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 50;
        int fibers = 50;
        String url = "https://reqres.in/api/users/2";


        IEmitterTask<ApacheHttpRequest<String>> requestTask = publisherChannel -> {
            for(int i=0; i<requests; i++){

                try {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("name", "pippo"+i);
                    payload.put("job", "pippo"+i);

                    byte[] bytePayload = new ObjectMapper().writeValueAsBytes(payload);

                    BasicHttpEntity httpEntity = new BasicHttpEntity();
                    httpEntity.setContent(new ByteArrayInputStream(bytePayload));
                    httpEntity.setContentLength(bytePayload.length);
                    httpEntity.setContentType("application/json");


                    HttpPut request = new HttpPut();
                    request.setURI(URI.create(url));
                    request.setEntity(httpEntity);

                    ApacheHttpRequest<String> okHttpRequest = new ApacheHttpRequest<>(requestPrefix+i, request, dataPrefix+i);

                    publisherChannel.send(okHttpRequest);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



            }
        };

        LinkedTransferQueue<ApacheHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

        IFlow currentFlow = QuasarFlow.newFlow()
                .broadcastEmitter(requestTask)
                .map(emitter -> new ApacheHttpProcessor<>(emitter)
                        .processWithFanIn(fibers))
                .addConsumer()
                .consume(responseQueue::add)
                .start();


        List<ApacheHttpResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("Executed "+requests+" requests in parallel ("+fibers+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
        System.out.println("Requests summed execution time: "+results.stream().mapToLong(ApacheHttpResponse::getExecution).sum()+" ms");

        assertEquals(results.size(), requests);

        for(ApacheHttpResponse<String> resp : results){
            String idx0 = resp.getAttachedDatas().split(dataPrefix)[1];
            String idx1 = resp.getRequestId().split(requestPrefix)[1];
            assertEquals(idx0, idx1);
        }

        assertEquals(results.size(), requests);

        //currentFlow.destroy();

    }

}
