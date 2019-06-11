package it.enryold.quasarflow.io.http.clients.ahc;

import co.paralleluniverse.fibers.httpclient.FiberHttpClient;
import it.enryold.quasarflow.abstracts.AbstractIOProcessor;
import it.enryold.quasarflow.interfaces.IEmitter;
import it.enryold.quasarflow.interfaces.IOProcessorTask;
import it.enryold.quasarflow.io.http.clients.ahc.models.ApacheHttpRequest;
import it.enryold.quasarflow.io.http.clients.ahc.models.ApacheHttpResponse;
import it.enryold.quasarflow.models.utils.QRoutingKey;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;

public class ApacheHttpProcessor<T> extends AbstractIOProcessor<ApacheHttpRequest<T>, ApacheHttpResponse<T>> {

    private FiberHttpClient client = new FiberHttpClient(defaultClient());

    public ApacheHttpProcessor(IEmitter<ApacheHttpRequest<T>> eEmitter, String name, QRoutingKey routingKey) {
        super(eEmitter, name, routingKey);
        init();
    }

    public ApacheHttpProcessor(IEmitter<ApacheHttpRequest<T>> eEmitter, QRoutingKey routingKey) {
        super(eEmitter, routingKey);
        init();
    }

    public ApacheHttpProcessor(IEmitter<ApacheHttpRequest<T>> eEmitter) {
        super(eEmitter);
        init();
    }

    public ApacheHttpProcessor<T> withAHCClient(CloseableHttpAsyncClient httpClient){
        this.client = new FiberHttpClient(httpClient);
        return this;
    }


    private void init(){
        processorTaskBuilder = () ->
                (IOProcessorTask<ApacheHttpRequest<T>, ApacheHttpResponse<T>>)
                        (elm) -> {

                            final CloseableHttpResponse response;

                            try {
                                long start = System.currentTimeMillis();
                                response = client.execute(elm.getRequest());
                                long execution = (System.currentTimeMillis() - start);
                                int status = response.getStatusLine().getStatusCode();

                                if (status >= 200 && status < 300) {
                                    log("HTTP request to " + elm.getRequest().getURI() + " executed in " + execution + " ms with status: " + status);
                                    return ApacheHttpResponse.success(elm.getRequestId(), execution, response, elm.getAttachedDatas());
                                } else {
                                    error("HTTP request to " + elm.getRequest().getURI() + " FAIL in " + execution + " ms with status: " + status);
                                    return ApacheHttpResponse.error(elm.getRequestId(), execution, response, elm.getAttachedDatas());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                error("HTTP request ERROR:" + e.getMessage());
                                throw new RuntimeException(e);
                            }

                        };
    }


    private static CloseableHttpAsyncClient defaultClient(){
        int timeout = 10_000;
        int maxRequests = 100_000;
        int threads = 16;

        try {
            final DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(IOReactorConfig.custom()
                    .setConnectTimeout(timeout)
                    .setIoThreadCount(threads)
                    .setSoTimeout(timeout).build());
            final PoolingNHttpClientConnectionManager mgr = new PoolingNHttpClientConnectionManager(ioReactor);
            mgr.setDefaultMaxPerRoute(maxRequests);
            mgr.setMaxTotal(maxRequests);

            return
                    HttpAsyncClientBuilder
                            .create()
                            .setConnectionManager(mgr)
                            .setDefaultRequestConfig( RequestConfig
                                    .custom()
                                    .setSocketTimeout(timeout)
                                    .setConnectTimeout(timeout)
                                    .setConnectionRequestTimeout(timeout)
                                    .build())
                            .build();
        } catch (IOReactorException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cannot instantiate CloseableHttpAsyncClient");
    }
}
