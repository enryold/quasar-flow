package it.enryold.quasarflow;

import it.enryold.quasarflow.interfaces.IEmitterTask;
import it.enryold.quasarflow.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

public class TestUtils {


    protected static final long MEGABYTE = 1024L * 1024L;


    protected static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    protected IEmitterTask<String> stringsEmitterTask(int max)
    {
        QuasarFlow.newFlow()
                .broadcastEmitter(null) // BUILD A BROADCAST EMITTER FROM TASK
                .addProcessor(p -> { // ADD A PROCESSOR
                    p.process() // PROCESS
                            .addConsumer(c -> // ADD A CONSUMER
                                    c.consume(str -> System.out.println(str))); // CONSUME WITH CONSUMER TASK
                })
                .start();

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

    protected <T>List<T> getResults(LinkedTransferQueue<T> channel)
    {
        List<T> results = new ArrayList<>();
        channel.drainTo(results);
        return results;
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
