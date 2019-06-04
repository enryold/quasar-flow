package it.enryold.quasarflow.io.http.interfaces;

public interface IQResponse<DataType, ResponseType> {


    long getExecution();
    boolean isFailure();
    ResponseType getResponse();
    DataType getAttachedDatas();
    String getRequestId();
}
