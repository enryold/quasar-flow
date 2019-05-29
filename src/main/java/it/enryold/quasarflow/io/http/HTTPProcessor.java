package it.enryold.quasarflow.io.http;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import com.squareup.okhttp.OkHttpClient;
import it.enryold.quasarflow.abstracts.AbstractIOProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IOProcessorAsyncTask;
import it.enryold.quasarflow.io.http.models.QHTTPRequest;
import it.enryold.quasarflow.io.http.models.QHTTPRequestCallback;
import it.enryold.quasarflow.io.http.models.QHTTPResponse;

public class HTTPProcessor<T> extends AbstractIOProcessor<QHTTPRequest<T>, QHTTPResponse<T>> {

    private OkHttpClient okHttpClient = new FiberOkHttpClient();

    public HTTPProcessor(IEmitter<QHTTPRequest<T>> eEmitter, String name, String routingKey) {
        super(eEmitter, name, routingKey);
        init();
    }

    public HTTPProcessor(IEmitter<QHTTPRequest<T>> eEmitter, String routingKey) {
        super(eEmitter, routingKey);
        init();
    }

    public HTTPProcessor(IEmitter<QHTTPRequest<T>> eEmitter) {
        super(eEmitter);
        init();
    }


    private void init(){
        processorAsyncTaskBuilder = () ->
                (IOProcessorAsyncTask<QHTTPRequest<T>, QHTTPResponse<T>>)
                        (elm, sendPort) -> okHttpClient.newCall(elm.getRequest())
                                .enqueue(new QHTTPRequestCallback<>(elm.getAttachedDatas(),qhttpResponse -> {
                                    try {
                                        sendPort.send(qhttpResponse);
                                    } catch (SuspendExecution | InterruptedException suspendExecution) {
                                        suspendExecution.printStackTrace();
                                    }
                                }));
    }
}
