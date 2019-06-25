package it.enryold.quasarflow;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.httpclient.FiberHttpClient;
import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import it.enryold.quasarflow.chain.FiberChain;
import it.enryold.quasarflow.chain.interfaces.IChain;
import it.enryold.quasarflow.chain.interfaces.IChainInjector;
import it.enryold.quasarflow.chain.io.http.ApacheChainFunction;
import it.enryold.quasarflow.chain.io.http.ApacheMultiChainFunction;
import it.enryold.quasarflow.chain.io.http.OkHttpChainFunction;
import it.enryold.quasarflow.chain.io.http.OkHttpMultiChainFunction;
import it.enryold.quasarflow.io.http.clients.ahc.models.ApacheHttpRequest;
import it.enryold.quasarflow.io.http.clients.ahc.models.ApacheHttpResponse;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOFiberChainTests extends TestUtils {



    @AfterEach
    public void afterEach(){

        this.printRuntime();
    }


    private FiberHttpClient apacheClient(){
        int timeout = 10_000;
        int maxRequests = 100_000;
        int threads = 16;

        try {
            final DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(IOReactorConfig.custom()
                    .setConnectTimeout(timeout)
                    .setIoThreadCount(threads)
                    .setSoTimeout(timeout).build());
            final PoolingNHttpClientConnectionManager mgr = new PoolingNHttpClientConnectionManager(ioReactor);
            mgr.setDefaultMaxPerRoute(maxRequests);
            mgr.setMaxTotal(maxRequests);

            return
                    new FiberHttpClient(HttpAsyncClientBuilder
                            .create()
                            .setConnectionManager(mgr)
                            .setDefaultRequestConfig( RequestConfig
                                    .custom()
                                    .setSocketTimeout(timeout)
                                    .setConnectTimeout(timeout)
                                    .setConnectionRequestTimeout(timeout)
                                    .build())
                            .build());
        } catch (IOReactorException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cannot instantiate CloseableHttpAsyncClient");
    }


    private FiberOkHttpClient okHttpClient(){
        int timeout = 10;
        int maxRequests = 100_000;

        FiberOkHttpClient client = new FiberOkHttpClient();

        client.getDispatcher().setMaxRequests(maxRequests);
        client.getDispatcher().setMaxRequestsPerHost(maxRequests);

        client.setRetryOnConnectionFailure(false);
        client.setConnectTimeout(timeout, TimeUnit.SECONDS);
        client.setReadTimeout(timeout, TimeUnit.SECONDS);
        client.setWriteTimeout(timeout, TimeUnit.SECONDS);
        return client;
    }



    @Test
    public void testOkHttp() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 10;
        String url = "https://reqres.in/api/users/2";

        FiberOkHttpClient client = okHttpClient();
        LinkedTransferQueue<OkHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();


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

                FiberChain.init("request", okHttpRequest)
                        .transform("okHttp", new OkHttpChainFunction<>(client))
                        .consume("response", responseQueue::add);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }



        List<OkHttpResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("SYNC: Executed "+requests+" requests in parallel ("+requests+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
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
    public void testOkHttpSyncLoad() throws InterruptedException {

        String requestPrefix = "REQ";
        String dataPrefix = "DATA";

        int requests = 50;
        String url = "https://reqres.in/api/users/2";

        FiberOkHttpClient client = okHttpClient();
        LinkedTransferQueue<OkHttpResponse<String>> responseQueue = resultQueue();

        long startTime = System.currentTimeMillis();

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



                FiberChain.initAsync("request", okHttpRequest,
                        chain -> chain.transform("okHttp", new OkHttpChainFunction<>(client))
                        .consume("response", responseQueue::add));


            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }







        List<OkHttpResponse<String>> results = this.getResults(responseQueue, requests, 10, TimeUnit.SECONDS);

        System.out.println("SYNC: Executed "+requests+" requests in parallel ("+requests+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
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
        String url = "https://reqres.in/api/users/2";

        long startTime = System.currentTimeMillis();

        LinkedTransferQueue<ApacheHttpResponse<String>> responseQueue = resultQueue();

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

                FiberChain.initAsync("request", okHttpRequest, chain ->
                        chain
                        .transform("apache", new ApacheChainFunction<>(apacheClient()))
                        .consume("response", responseQueue::add));

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }



        }





        List<ApacheHttpResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("Executed "+requests+" requests in parallel ("+requests+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
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
        String url = "https://reqres.in/api/users/2";


        LinkedTransferQueue<ApacheHttpResponse<String>> responseQueue = resultQueue();
        long startTime = System.currentTimeMillis();


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

                FiberChain.initAsync("request", okHttpRequest, chain ->
                        chain.transform("apache", new ApacheChainFunction<>(apacheClient()))
                        .consume("response", responseQueue::add));

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }



        }






        List<ApacheHttpResponse<String>> results = this.getResults(responseQueue, requests, 3, TimeUnit.SECONDS);

        System.out.println("Executed "+requests+" requests in parallel ("+requests+" fibers) in: "+(System.currentTimeMillis()-startTime)+" ms");
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
