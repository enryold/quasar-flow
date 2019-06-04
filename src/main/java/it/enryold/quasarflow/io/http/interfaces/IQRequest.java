package it.enryold.quasarflow.io.http.interfaces;

public interface IQRequest<DataType, RequestType> {


    String getRequestId();

    RequestType getRequest();

    DataType getAttachedDatas();
}
