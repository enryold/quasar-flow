package it.enryold.quasarflow;

import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class TestUtils {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final long MEGABYTE = 1024L * 1024L;


    protected static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    protected IEmitterTask<String> stringsEmitterTask(int max)
    {
        return publisherChannel -> {
            for(int i = 0; i<max; i++){ publisherChannel.send("String"+i); } };
    }


    protected IEmitterTask<String> tenByteStringsEmitterTask(int max)
    {
        return publisherChannel -> {
            for(int i = 0; i<max; i++){ publisherChannel.send("0123456789"); } };
    }

    protected <T>LinkedTransferQueue<T> resultQueue()
    {
        return new LinkedTransferQueue<>();
    }

    protected <T>List<T> getResults(LinkedTransferQueue<T> queue, int expectedElements, int timeOut, TimeUnit unit) throws InterruptedException {
        T elm;
        List<T> results = new ArrayList<>();
        long deadline = System.nanoTime() + unit.toNanos(timeOut);

        do{
            elm = queue.poll(1, TimeUnit.NANOSECONDS);

            if (elm == null) { // not enough elements immediately available; will have to poll
                elm = queue.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                if (elm == null) {
                    break; // we already waited enough, and there are no more elements in sight
                }
                results.add(elm);
            }else{
                results.add(elm);
            }
        }
        while (results.size() < expectedElements);

        return results;
    }


    public void printRuntime(){
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        log.info("Max memory is: " + Runtime.getRuntime().maxMemory());
        log.info("Used memory is bytes: " + memory);
        log.info("Used memory is megabytes: "
                + bytesToMegabytes(memory));
    }

    protected List<User> userGenerator(int max)
    {
        List<User> arrayList = new ArrayList<>();
        for(int i = 0; i<max; i++){
            User u = new User();
            u.setId(""+i);
            u.setName("Name"+i);
            arrayList.add(u);
        }
        return arrayList;
    }
}
