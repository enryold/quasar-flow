package it.enryold.quasarflow.chain;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import it.enryold.quasarflow.chain.interfaces.*;

import java.util.concurrent.ExecutionException;

public class FiberChain<E> implements IChain<E> {

    private String chainName;
    private E object;

    private FiberChain(String name){
        this.chainName = name;
    }

    private FiberChain(String name, E obj){
        this.chainName = name;
        this.object = obj;
    }

    public static <E> FiberChain<E> init(String name, E obj){
        return new FiberChain<>(name, obj);
    }

    public static <E> void initAsync(String name, E obj, IChainInjector<E> injector){
        new Fiber<>(() -> injector.inject(new FiberChain<>(name, obj))).start();
    }

    @Suspendable
    public E getObject(){
        return object;
    }

    @Suspendable
    public <T> FiberChain<T> transform(String name, IChainFunction<E, T> fn){

        if(object == null){
            throw new RuntimeException("FiberChain: "+name+":transform input object is null");
        }

        T obj;

        try {
            obj = new Fiber<>(() -> fn.apply(object))
                    .start()
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            obj = null;
        }

        return new FiberChain<>(chainName+"-"+name, obj);
    }




    @Suspendable
    public void consume(String name, IChainConsumer<E> fn){

        if(object == null){
            throw new RuntimeException("FiberChain: "+name+":consume input object is null");
        }

        new Fiber<>(() -> fn.consume(object)).start();
    }

    @Suspendable
    public void consume(String name, IChainConsumer<E> fn, IChainSplitter<E> splitter){

        if(object == null){
            throw new RuntimeException("FiberChain: "+name+":consume input object is null");
        }

        new Fiber<>(() -> {
            for(E chunk : splitter.split(object)){
                new Fiber<>(() -> fn.consume(chunk)).start();
            }
        }).start();
    }


}
