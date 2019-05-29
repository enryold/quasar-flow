package it.enryold.quasarflow.io.http.models;

import com.squareup.okhttp.Response;

public class QHTTPResponse<T> {

    private T attachedDatas;
    private Response response;
    private String requestId;
    private long execution;
    private boolean failure;


    private QHTTPResponse(String requestId, long execution, Response response, boolean failure, T attachedDatas){
        this.requestId = requestId;
        this.response = response;
        this.execution = execution;
        this.attachedDatas = attachedDatas;
        this.failure = failure;
    }

    public long getExecution() {
        return execution;
    }

    public boolean isFailure() {
        return failure;
    }

    public Response getResponse() {
        return response;
    }

    public T getAttachedDatas() {
        return attachedDatas;
    }

    public String getRequestId() {
        return requestId;
    }


    public static<T>  QHTTPResponse<T> error(String requestId, long execution, T attachedDatas){
        return new QHTTPResponse<>(requestId,  execution, null, true, attachedDatas);
    }

    public static<T>  QHTTPResponse<T> success(String requestId, long execution, Response response, T attachedDatas){
        return new QHTTPResponse<>(requestId,  execution, response, false, attachedDatas);
    }

}
