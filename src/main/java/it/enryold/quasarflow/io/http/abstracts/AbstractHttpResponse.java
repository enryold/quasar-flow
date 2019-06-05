package it.enryold.quasarflow.io.http.abstracts;

import it.enryold.quasarflow.io.http.interfaces.IQResponse;

import java.util.List;
import java.util.Map;

public abstract class AbstractHttpResponse<T, R> implements IQResponse<T, R> {

    private T attachedDatas;
    private String requestId;
    private long execution;
    private boolean failure;
    protected byte[] body;
    protected int status;
    protected Map<String, List<String>> headers;


    public AbstractHttpResponse(String requestId, long execution, R response, boolean failure, T attachedDatas){
        this.requestId = requestId;
        this.execution = execution;
        this.attachedDatas = attachedDatas;
        this.failure = failure;
        map(response);
    }

    public long getExecution() {
        return execution;
    }

    public boolean isFailure() {
        return failure;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public T getAttachedDatas() {
        return attachedDatas;
    }

    public String getRequestId() {
        return requestId;
    }



}
