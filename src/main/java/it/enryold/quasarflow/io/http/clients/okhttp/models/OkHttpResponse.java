package it.enryold.quasarflow.io.http.clients.okhttp.models;

import com.squareup.okhttp.Response;
import it.enryold.quasarflow.io.http.abstracts.AbstractHttpResponse;

import java.io.IOException;

public class OkHttpResponse<T> extends AbstractHttpResponse<T, Response> {


    public OkHttpResponse(String requestId, long execution, Response response, boolean failure, T attachedDatas) {
        super(requestId, execution, response, failure, attachedDatas);
    }

    @Override
    public void map(Response response) {

        this.status = response.code();
        this.headers = response.headers().toMultimap();
        try {
            this.body = response.body().bytes();
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static<T> OkHttpResponse<T> error(String requestId, long execution, Response response, T attachedDatas){
        return new OkHttpResponse<>(requestId,  execution, response, true, attachedDatas);
    }

    public static<T> OkHttpResponse<T> success(String requestId, long execution, Response response, T attachedDatas){
        return new OkHttpResponse<>(requestId,  execution, response, false, attachedDatas);
    }

}
