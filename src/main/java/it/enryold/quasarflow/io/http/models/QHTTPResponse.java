package it.enryold.quasarflow.io.http.models;

import com.squareup.okhttp.Response;

public class QHTTPResponse {

    private Response response;
    private boolean isError;
    private String requestId;
    private long execution;


    private QHTTPResponse(String requestId, boolean isError, long execution, Response response){
        this.requestId = requestId;
        this.isError = isError;
        this.response = response;
        this.execution = execution;
    }

    public long getExecution() {
        return execution;
    }

    public Response getResponse() {
        return response;
    }

    public boolean isError() {
        return isError;
    }

    public String getRequestId() {
        return requestId;
    }

    public static  QHTTPResponse error(String requestId, long execution){
        return new QHTTPResponse(requestId, true, execution, null);
    }

    public static  QHTTPResponse error(String requestId, long execution, Response response){
        return new QHTTPResponse(requestId, true, execution, response);
    }

    public static  QHTTPResponse success(String requestId, long execution, Response response){
        return new QHTTPResponse(requestId, false, execution, response);
    }

}
