package it.enryold.quasarflow.io.http.interfaces;

import java.util.List;
import java.util.Map;

public interface IQResponse<DataType, ResponseType> {


    long getExecution();
    boolean isFailure();
    byte[] getBody();
    Map<String, List<String>> getHeaders();
    DataType getAttachedDatas();
    String getRequestId();
    int getStatus();

    void map(ResponseType responseType);
}
