package it.enryold.quasarflow.chain.io.http;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.httpclient.FiberHttpClient;
import it.enryold.quasarflow.chain.interfaces.IChainFunction;
import it.enryold.quasarflow.io.http.clients.ahc.models.ApacheHttpRequest;
import it.enryold.quasarflow.io.http.clients.ahc.models.ApacheHttpResponse;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ApacheMultiChainFunction<T> implements IChainFunction<List<ApacheHttpRequest<T>>, List<ApacheHttpResponse<T>>> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FiberHttpClient fiberHttpClient;
    private final boolean didLogRequests;

    public ApacheMultiChainFunction(FiberHttpClient fiberHttpClient, boolean didLogRequests){
        this.fiberHttpClient = fiberHttpClient;
        this.didLogRequests = didLogRequests;
    }

    public ApacheMultiChainFunction(FiberHttpClient fiberHttpClient){
        this.fiberHttpClient = fiberHttpClient;
        this.didLogRequests = false;
    }

    @Suspendable
    private void logRequest(String message){
        if(didLogRequests){
            log.info(message);
        }else{
            log.debug(message);
        }
    }


    @Suspendable
    public ApacheHttpResponse<T> doRequest(ApacheHttpRequest<T> elm) {
        long start = System.currentTimeMillis();

        final CloseableHttpResponse response;

        try {
            response = fiberHttpClient.execute(elm.getRequest());
            long execution = (System.currentTimeMillis() - start);
            int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                logRequest("HTTP request with status: "+status+" to " + elm.getRequest().getURI() + " executed in " + execution + " ms with status: " + status);
                return ApacheHttpResponse.success(elm.getRequestId(), execution, response, elm.getAttachedDatas());
            } else {
                log.error("HTTP request with status: "+status+" to " + elm.getRequest().getURI() + " FAIL in " + execution + " ms with status: " + status);
                return ApacheHttpResponse.error(elm.getRequestId(), execution, response, elm.getAttachedDatas());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("HTTP request ERROR:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Suspendable
    public List<ApacheHttpResponse<T>> apply(List<ApacheHttpRequest<T>> apacheHttpRequests) {
        List<ApacheHttpResponse<T>> responses = new ArrayList<>();
        for(ApacheHttpRequest<T> request : apacheHttpRequests){
            responses.add(doRequest(request));
        }
        return responses;
    }
}
