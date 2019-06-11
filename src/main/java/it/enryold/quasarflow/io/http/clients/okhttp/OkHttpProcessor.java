package it.enryold.quasarflow.io.http.clients.okhttp;

import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import it.enryold.quasarflow.abstracts.AbstractIOProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IOProcessorTask;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpResponse;
import it.enryold.quasarflow.io.http.consts.QHttpConsts;
import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpProcessor<T> extends AbstractIOProcessor<OkHttpRequest<T>, OkHttpResponse<T>> {

    private OkHttpClient okHttpClient = defaultClient();
    private boolean didLogRequests = false;

    public OkHttpProcessor(IEmitter<OkHttpRequest<T>> eEmitter, String name, QRoutingKey routingKey) {
        super(eEmitter, name, routingKey);
        init();
    }

    public OkHttpProcessor(IEmitter<OkHttpRequest<T>> eEmitter, QRoutingKey routingKey) {
        super(eEmitter, routingKey);
        init();
    }

    public OkHttpProcessor(IEmitter<OkHttpRequest<T>> eEmitter) {
        super(eEmitter);
        init();
    }

    public OkHttpProcessor<T> withOkHttpClient(OkHttpClient okHttpClient){
        this.okHttpClient = okHttpClient;
        return this;
    }

    public OkHttpProcessor<T> withDidLogRequests(boolean didLogRequests){
        this.didLogRequests = didLogRequests;
        return this;
    }


    private void init(){
        initSync();
    }



    private void initSync(){
        processorTaskBuilder = () ->

                (IOProcessorTask<OkHttpRequest<T>, OkHttpResponse<T>>)
                        (elm) -> {

                            long start = System.currentTimeMillis();

                            try {
                                final Response response = okHttpClient.newCall(elm.getRequest()).execute();
                                long execution = System.currentTimeMillis()-start;


                                if (response.code() >= 200 && response.code() < 300) {
                                    logRequest("["+elm.getRequestId()+"] HTTP async request to "+response.request().url().toString()+" executed in "+execution+" ms ");

                                    return OkHttpResponse.success(elm.getRequestId(), execution, response, elm.getAttachedDatas());
                                }else{
                                    log.error("["+elm.getRequestId()+"] HTTP async request to "+response.request().url().toString()+" FAIL executed in "+execution+" ms ");

                                    return OkHttpResponse.error(elm.getRequestId(), execution, response, elm.getAttachedDatas());
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                                log.error("["+elm.getRequestId()+"] HTTP async request FAIL executed in "+(System.currentTimeMillis()-start)+" ms, ex: "+e.getMessage());
                                return OkHttpResponse.error(elm.getRequestId(), (System.currentTimeMillis()-start), null, elm.getAttachedDatas());
                            }
                        };

    }


    private void logRequest(String message){
        if(didLogRequests){
            log.info(message);
        }else{
            log.debug(message);
        }
    }


    private static OkHttpClient defaultClient(){

        int timeout = 10;
        int maxRequests = 100_000;

        OkHttpClient client = new FiberOkHttpClient();

        client.getDispatcher().setMaxRequests(maxRequests);
        client.getDispatcher().setMaxRequestsPerHost(maxRequests);

        client.setRetryOnConnectionFailure(false);
        client.setConnectTimeout(timeout, TimeUnit.SECONDS);
        client.setReadTimeout(timeout, TimeUnit.SECONDS);
        client.setWriteTimeout(timeout, TimeUnit.SECONDS);

        return client;
    }
}
