package it.enryold.quasarflow.io.http.ahc.models;

import com.squareup.okhttp.Request;
import it.enryold.quasarflow.io.http.consts.QHttpConsts;
import it.enryold.quasarflow.io.http.interfaces.IQRequest;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.UUID;

public class ApacheHttpRequest<T>  implements IQRequest<T, HttpUriRequest> {

    private T attachedDatas;
    private String requestId;
    private HttpUriRequest request;

    public ApacheHttpRequest(HttpUriRequest request){
        this.request = request;
        this.requestId = UUID.randomUUID().toString();
        addRequestIdHeader();
    }

    public ApacheHttpRequest(String requestId, HttpUriRequest request){
        this.request = request;
        this.requestId = requestId;
        addRequestIdHeader();
    }

    public ApacheHttpRequest(String requestId, HttpUriRequest request, T attachedDatas){
        this.request = request;
        this.requestId = requestId;
        this.attachedDatas = attachedDatas;
        addRequestIdHeader();
    }

    private void addRequestIdHeader(){
        this.request.addHeader(QHttpConsts.REQUEST_HEADER, requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public HttpUriRequest getRequest() {
        return request;
    }

    public T getAttachedDatas() {
        return attachedDatas;
    }
}
