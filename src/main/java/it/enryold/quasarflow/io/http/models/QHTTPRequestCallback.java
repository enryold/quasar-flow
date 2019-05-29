package it.enryold.quasarflow.io.http.models;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import it.enryold.quasarflow.io.http.consts.QHttpConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class QHTTPRequestCallback<T> implements Callback
{
    private Logger log = LoggerFactory.getLogger(getClass());
    private long start;
    private Consumer<QHTTPResponse<T>> consumer;
    private T attachedDatas;

    public QHTTPRequestCallback(T attachedDatas, Consumer<QHTTPResponse<T>> consumer) {
        this.consumer = consumer;
        this.attachedDatas = attachedDatas;
        start = System.currentTimeMillis();
    }

    @Override
    public void onFailure(Request request, IOException e) {

        long execution = (System.currentTimeMillis()-start);

        String requestId = request.header(QHttpConsts.REQUEST_HEADER);

        log.error("["+requestId+"] HTTP async request to "+request.url().toString()+" executed in "+execution+" ms with exception: "+e.getMessage());

        consumer.accept(QHTTPResponse.error(requestId, execution, attachedDatas));
    }

    @Override
    public void onResponse(Response response) {

        long execution = (System.currentTimeMillis()-start);

        String requestId = response.request().header(QHttpConsts.REQUEST_HEADER);

        log.info("["+requestId+"] HTTP async request to "+response.request().url().toString()+" executed in "+execution+" ms ");

        consumer.accept(QHTTPResponse.success(requestId, execution, response, attachedDatas));

    }


}