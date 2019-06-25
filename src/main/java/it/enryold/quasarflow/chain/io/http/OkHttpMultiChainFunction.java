package it.enryold.quasarflow.chain.io.http;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import com.squareup.okhttp.Response;
import it.enryold.quasarflow.chain.interfaces.IChainFunction;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OkHttpMultiChainFunction<T> implements IChainFunction<List<OkHttpRequest<T>>, List<OkHttpResponse<T>>> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FiberOkHttpClient fiberOkHttpClient;
    private final boolean didLogRequests;

    public OkHttpMultiChainFunction(FiberOkHttpClient fiberOkHttpClient, boolean didLogRequests){
        this.fiberOkHttpClient = fiberOkHttpClient;
        this.didLogRequests = didLogRequests;
    }

    public OkHttpMultiChainFunction(FiberOkHttpClient fiberOkHttpClient){
        this.fiberOkHttpClient = fiberOkHttpClient;
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
    private OkHttpResponse<T> doRequest(OkHttpRequest<T> elm) {
        long start = System.currentTimeMillis();

        try {
            final Response response = fiberOkHttpClient.newCall(elm.getRequest()).execute();
            long execution = System.currentTimeMillis()-start;


            if (response.code() >= 200 && response.code() < 300) {
                logRequest("["+elm.getRequestId()+"] HTTP async request with status "+response.code()+" to "+response.request().url().toString()+" executed in "+execution+" ms ");

                return OkHttpResponse.success(elm.getRequestId(), execution, response, elm.getAttachedDatas());
            }else{
                log.error("["+elm.getRequestId()+"] HTTP async request with status "+response.code()+" to "+response.request().url().toString()+" FAIL executed in "+execution+" ms ");

                return OkHttpResponse.error(elm.getRequestId(), execution, response, elm.getAttachedDatas());
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("["+elm.getRequestId()+"] HTTP async request FAIL executed in "+(System.currentTimeMillis()-start)+" ms, ex: "+e.getMessage());
            return OkHttpResponse.error(elm.getRequestId(), (System.currentTimeMillis()-start), null, elm.getAttachedDatas());
        }
    }

    @Override
    @Suspendable
    public List<OkHttpResponse<T>> apply(List<OkHttpRequest<T>> okHttpRequests) {

        List<OkHttpResponse<T>> responses = new ArrayList<>();
        for(OkHttpRequest<T> request : okHttpRequests){
            responses.add(doRequest(request));
        }
        return responses;
    }
}
