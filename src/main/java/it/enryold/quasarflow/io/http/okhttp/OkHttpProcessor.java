package it.enryold.quasarflow.io.http.okhttp;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import co.paralleluniverse.strands.SuspendableRunnable;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import it.enryold.quasarflow.abstracts.AbstractIOProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IOProcessorAsyncTask;
import it.enryold.quasarflow.io.http.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.io.http.okhttp.models.OkHttpRequestCallback;
import it.enryold.quasarflow.io.http.okhttp.models.OkHttpResponse;
import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.io.IOException;

public class OkHttpProcessor<T> extends AbstractIOProcessor<OkHttpRequest<T>, OkHttpResponse<T>> {

    private OkHttpClient okHttpClient = new FiberOkHttpClient();

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
                                }));
    }

    private void initSync(){
        processorAsyncTaskBuilder = () ->

                (IOProcessorAsyncTask<OkHttpRequest<T>, OkHttpResponse<T>>)
                        (elm, sendPort) -> new Fiber<Void>((SuspendableRunnable) () -> {
                            final OkHttpRequestCallback<T> callback = new OkHttpRequestCallback<>(elm.getAttachedDatas(), tOkHttpResponse -> {
                                try {
                                    sendPort.send(tOkHttpResponse);
                                } catch (SuspendExecution | InterruptedException suspendExecution) {
                                    suspendExecution.printStackTrace();
                                }
                            });

                            try {
                                Response response = okHttpClient.newCall(elm.getRequest()).execute();
                                callback.onResponse(response);
                            } catch (IOException e) {
                                e.printStackTrace();
                                callback.onFailure(elm.getRequest(),e);
                            }

                        }).start();

    }
}
