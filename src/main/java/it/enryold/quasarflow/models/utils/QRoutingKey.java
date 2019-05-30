package it.enryold.quasarflow.models.utils;

public class QRoutingKey {

    private String routingKey;

    private QRoutingKey(String key){
        this.routingKey = key;
    }

    public static QRoutingKey withKey(String key){
        return new QRoutingKey(key);
    }

    public static QRoutingKey broadcast(){
        return new QRoutingKey("BROADCAST");
    }

    public String getKey(){
        return routingKey;
    }
}
