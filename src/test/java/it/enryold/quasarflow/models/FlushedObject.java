package it.enryold.quasarflow.models;

import java.util.Date;

public class FlushedObject<T> {

    private T obj;
    private long creation;

    public FlushedObject(T obj) {
        this.obj = obj;
        this.creation = new Date().getTime();
    }

    public T getObj() {
        return obj;
    }

    public long getCreation() {
        return creation;
    }
}
