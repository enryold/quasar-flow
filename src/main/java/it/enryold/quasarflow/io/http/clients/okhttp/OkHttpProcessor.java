package it.enryold.quasarflow.io.http.clients.okhttp;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import co.paralleluniverse.strands.SuspendableRunnable;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import it.enryold.quasarflow.abstracts.AbstractIOProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IOProcessorAsyncTask;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpRequestCallback;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpResponse;
import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpProcessor<T> extends AbstractIOProcessor<OkHttpRequest<T>, OkHttpResponse<T>> {

    private OkHttpClient okHttpClient = defaultClient();
    private boolean didLogRequests = false;

    public OkHttpProcessor(IEmitter<OkHttpRequest<T>> eEmitter, String name, QRoutingKey routingKey, boolean async) {
        super(eEmitter, name, routingKey);
        init(async);
    }

    public OkHttpProcessor(IEmitter<OkHttpRequest<T>> eEmitter, QRoutingKey routingKey, boolean async) {
        super(eEmitter, routingKey);
        init(async);
    }

    public OkHttpProcessor(IEmitter<OkHttpRequest<T>> eEmitter, boolean async) {
        super(eEmitter);
        init(async);
    }

    public OkHttpProcessor(IEmitter<OkHttpRequest<T>> eEmitter) {
        super(eEmitter);
        init(false);
    }

    public OkHttpProcessor<T> withOkHttpClient(OkHttpClient okHttpClient){
        this.okHttpClient = okHttpClient;
        return this;
    }

    public OkHttpProcessor<T> withDidLogRequests(boolean didLogRequests){
        this.didLogRequests = didLogRequests;
        return this;
    }


    private void init(boolean async){
        if(async)
            initAsync();
        else
            initSync();
    }


    private void initAsync(){
        processorAsyncTaskBuilder = () ->
                (IOProcessorAsyncTask<OkHttpRequest<T>, OkHttpResponse<T>>)
                        (elm, sendPort) -> okHttpClient.newCall(elm.getRequest())
                                .enqueue(new OkHttpRequestCallback<>(elm.getAttachedDatas(), okHttpResponse -> {
                                    try {
                                        sendPort.send(okHttpResponse);
                                    } catch (SuspendExecution | InterruptedException suspendExecution) {
                                        suspendExecution.printStackTrace();
                                    }
                                }, didLogRequests));
    }

    private void initSync(){
        processorAsyncTaskBuilder = () ->

                (IOProcessorAsyncTask<OkHttpRequest<T>, OkHttpResponse<T>>)
                        (elm, sendPort) -> {

                            new Fiber<Void>((SuspendableRunnable) () -> {

                                final OkHttpRequestCallback<T> callback = new OkHttpRequestCallback<>(elm.getAttachedDatas(), tOkHttpResponse -> {
                                    try {
                                        sendPort.send(tOkHttpResponse);
                                    } catch (SuspendExecution | InterruptedException suspendExecution) {
                                        suspendExecution.printStackTrace();
                                    }
                                }, didLogRequests);

                                try {
                                    final Response response = okHttpClient.newCall(elm.getRequest()).execute();

                                    if (response.code() >= 200 && response.code() < 300) {
                                        callback.onResponse(response);
                                    }else{
                                        callback.onFailure(elm.getRequest(), response, new IOException("Status is:"+response.code()));
                                    }

                                    } catch (IOException e) {
                                    e.printStackTrace();
                                    callback.onFailure(elm.getRequest(),e);
                                }

                            }).start();
                        };

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
