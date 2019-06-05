package it.enryold.quasarflow.io.http.clients.okhttp.models;

import com.squareup.okhttp.Request;
import it.enryold.quasarflow.io.http.consts.QHttpConsts;
import it.enryold.quasarflow.io.http.interfaces.IQRequest;

import java.util.UUID;

public class OkHttpRequest<T> implements IQRequest<T, Request> {

    private T attachedDatas;
    private String requestId;
    private Request request;

    public OkHttpRequest(Request request){
        this.request = request;
        this.requestId = UUID.randomUUID().toString();
        addRequestIdHeader();
    }

    public OkHttpRequest(String requestId, Request request){
        this.request = request;
        this.requestId = requestId;
        addRequestIdHeader();
    }

    public OkHttpRequest(String requestId, Request request, T attachedDatas){
        this.request = request;
        this.requestId = requestId;
        this.attachedDatas = attachedDatas;
        addRequestIdHeader();
    }

    private void addRequestIdHeader(){
        this.request = request.newBuilder()
                .addHeader(QHttpConsts.REQUEST_HEADER, requestId)
                .build();
    }

    public String getRequestId() {
        return requestId;
    }

    public Request getRequest() {
        return request;
    }

    public T getAttachedDatas() {
        return attachedDatas;
    }
}
