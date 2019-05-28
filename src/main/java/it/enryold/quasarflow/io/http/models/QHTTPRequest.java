package it.enryold.quasarflow.io.http.models;

import com.squareup.okhttp.Request;
import it.enryold.quasarflow.io.http.consts.QHttpConsts;

import java.util.UUID;

public class QHTTPRequest {

    private String requestId;
    private Request request;

    public QHTTPRequest(Request request){
        this.request = request;
        this.requestId = UUID.randomUUID().toString();
        addRequestIdHeader();
    }

    public QHTTPRequest(String requestId, Request request){
        this.request = request;
        this.requestId = requestId;
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
}
