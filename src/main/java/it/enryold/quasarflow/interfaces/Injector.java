package it.enryold.quasarflow.interfaces;

import java.util.function.Consumer;

public interface Injector<T> {

    void accept(T t);
}
