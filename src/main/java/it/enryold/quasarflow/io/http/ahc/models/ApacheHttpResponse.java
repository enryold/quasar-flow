package it.enryold.quasarflow.io.http.ahc.models;

import org.apache.http.client.methods.CloseableHttpResponse;

public class ApacheHttpResponse<T> {

    private T attachedDatas;
    private CloseableHttpResponse response;
    private String requestId;
    private long execution;
    private boolean failure;


    private ApacheHttpResponse(String requestId, long execution, CloseableHttpResponse response, boolean failure, T attachedDatas){
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

    public CloseableHttpResponse getResponse() {
        return response;
    }

    public T getAttachedDatas() {
        return attachedDatas;
    }

    public String getRequestId() {
        return requestId;
    }


    public static<T> ApacheHttpResponse<T> error(String requestId, long execution, T attachedDatas){
        return new ApacheHttpResponse<>(requestId,  execution, null, true, attachedDatas);
    }

    public static<T> ApacheHttpResponse<T> success(String requestId, long execution, CloseableHttpResponse response, T attachedDatas){
        return new ApacheHttpResponse<>(requestId,  execution, response, false, attachedDatas);
    }

}
