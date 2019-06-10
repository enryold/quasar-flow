package it.enryold.quasarflow.io.http.clients.okhttp.models;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import it.enryold.quasarflow.io.http.consts.QHttpConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class OkHttpRequestCallback<T> implements Callback
{
    private Logger log = LoggerFactory.getLogger(getClass());
    private long start;
    private Consumer<OkHttpResponse<T>> consumer;
    private T attachedDatas;
    private boolean didLogRequests;

    public OkHttpRequestCallback(T attachedDatas, Consumer<OkHttpResponse<T>> consumer) {
        this(attachedDatas, consumer, false);
    }

    public OkHttpRequestCallback(T attachedDatas, Consumer<OkHttpResponse<T>> consumer, boolean didLogRequests) {
        this.consumer = consumer;
        this.attachedDatas = attachedDatas;
        this.didLogRequests = didLogRequests;
        start = System.currentTimeMillis();
    }

    @Override
    public void onFailure(Request request, IOException e) {

        long execution = (System.currentTimeMillis()-start);

        String requestId = request.header(QHttpConsts.REQUEST_HEADER);

        log.error("["+requestId+"] HTTP async request to "+request.url().toString()+" executed in "+execution+" ms with exception: "+e.getMessage());

        consumer.accept(OkHttpResponse.error(requestId, execution, null ,attachedDatas));
    }

    public void onFailure(Request request, Response response, IOException e) {

        long execution = (System.currentTimeMillis()-start);

        String requestId = request.header(QHttpConsts.REQUEST_HEADER);

        log.error("["+requestId+"] HTTP async request to "+request.url().toString()+" executed in "+execution+" ms with exception: "+e.getMessage());

        consumer.accept(OkHttpResponse.error(requestId, execution, response, attachedDatas));
    }

    @Override
    public void onResponse(Response response) {

        long execution = (System.currentTimeMillis()-start);

        String requestId = response.request().header(QHttpConsts.REQUEST_HEADER);

        logRequest("["+requestId+"] HTTP async request to "+response.request().url().toString()+" executed in "+execution+" ms ");

        consumer.accept(OkHttpResponse.success(requestId, execution, response, attachedDatas));

    }


    private void logRequest(String message){
        if(didLogRequests){
            log.info(message);
        }else{
            log.debug(message);
        }
    }


}