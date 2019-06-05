package it.enryold.quasarflow.io.http.clients.ahc.models;

import com.amazonaws.util.IOUtils;
import it.enryold.quasarflow.io.http.abstracts.AbstractHttpResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApacheHttpResponse<T> extends AbstractHttpResponse<T, CloseableHttpResponse> {


    public ApacheHttpResponse(String requestId, long execution, CloseableHttpResponse response, boolean failure, T attachedDatas){
        super(requestId, execution, response, failure, attachedDatas);
    }

    @Override
    public void map(CloseableHttpResponse closeableHttpResponse) {
        this.headers = new HashMap<>();

        this.status = closeableHttpResponse.getStatusLine().getStatusCode();

        for(Header o : closeableHttpResponse.getAllHeaders()){
            List<String> list = headers.getOrDefault(o.getName(), new ArrayList<>());
            list.add(o.getValue());
            headers.put(o.getName(), list);
        }

        try {
            this.body = IOUtils.toByteArray(closeableHttpResponse.getEntity().getContent());
            closeableHttpResponse.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static<T> ApacheHttpResponse<T> error(String requestId, long execution, CloseableHttpResponse response, T attachedDatas){
        return new ApacheHttpResponse<>(requestId,  execution, response, true, attachedDatas);
    }

    public static<T> ApacheHttpResponse<T> success(String requestId, long execution, CloseableHttpResponse response, T attachedDatas){
        return new ApacheHttpResponse<>(requestId,  execution, response, false, attachedDatas);
    }

}
