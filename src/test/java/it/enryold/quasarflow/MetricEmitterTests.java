package it.enryold.quasarflow;


import it.enryold.quasarflow.interfaces.*;
import it.enryold.quasarflow.io.http.clients.okhttp.models.OkHttpRequest;
import it.enryold.quasarflow.models.utils.QSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class MetricEmitterTests extends TestUtils {


    @AfterEach
    public void afterEach(){
        this.printRuntime();


      
    }




    @Test
    public void testMetric() {

        // PARAMS
        int elements = 19;

        // EMITTER
        IEmitterTask<String> stringEmitter = stringsEmitterTask(elements);


        IFlow currentFlow;


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter, "stringEmitter")
                .addProcessor("stringProcessor")
                .process()
                .addConsumer("stringConsumer")
                .flow();

        currentFlow.print();


        System.out.println("-----------------\n");


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter, "stringEmitter")
                .addProcessor("stringProcessor1", p -> p.process().addConsumer("c1").consume(e -> {}))
                .addProcessor("stringProcessor2", p -> p.process().addConsumer("c2").consume(e -> {}))
                .flow();

        currentFlow.print();


        System.out.println("-----------------\n");


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter, "stringEmitter")
                .addProcessor("stringProcessor1", p -> p.process().addConsumer("c1").consume(e -> {}))
                .addProcessor("stringProcessor2", p -> p.process().addConsumer("c2").consume(e -> {}))
                .addProcessor("stringToIntProcessor")
                .process(() -> String::length)
                .map(emitter -> emitter
                        .addProcessor("intToStringProcessor")
                        .process(() -> integer -> "Length is: "+integer))
                .flow();

        currentFlow.print();


        System.out.println("-----------------\n");


        currentFlow = QuasarFlow.newFlow(QSettings.test())
                .broadcastEmitter(stringEmitter, "stringEmitter")
                .addProcessor("stringProcessor1",
                        p -> p
                                .process()
                                .addConsumer("c1")
                                .consume(e -> {}))
                .addProcessor("stringProcessor2",
                        p -> p
                                .process()
                                .addConsumer("c2")
                                .consume(e -> {}))
                .addProcessor("stringProcessor3",
                        p -> p
                                .process(() -> String::length)
                                .broadcast()
                                .addProcessor("intProcessor1",
                                    p1 -> p1
                                            .process()
                                            .addConsumer("cInt1")
                                            .consume(e -> {}))
                                .addProcessor("intProcessor2",
                                    p2 -> p2
                                            .process()
                                            .addConsumer("cInt2")
                                            .consume(e -> {})))

                .flow();

        currentFlow.print();




    }



}
