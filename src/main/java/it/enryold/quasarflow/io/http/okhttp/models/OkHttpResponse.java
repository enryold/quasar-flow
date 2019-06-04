package it.enryold.quasarflow.io.http.okhttp.models;

import com.squareup.okhttp.Response;

public class OkHttpResponse<T> {

    private T attachedDatas;
    private Response response;
    private String requestId;
    private long execution;
    private boolean failure;


    private OkHttpResponse(String requestId, long execution, Response response, boolean failure, T attachedDatas){
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


    public static<T> OkHttpResponse<T> error(String requestId, long execution, T attachedDatas){
        return new OkHttpResponse<>(requestId,  execution, null, true, attachedDatas);
    }

    public static<T> OkHttpResponse<T> success(String requestId, long execution, Response response, T attachedDatas){
        return new OkHttpResponse<>(requestId,  execution, response, false, attachedDatas);
    }

}
