package io.github.athingx.athing.thing.api.function;

public interface ExConsumer<T, X extends Throwable> {

    void accept(T t) throws X;

}
