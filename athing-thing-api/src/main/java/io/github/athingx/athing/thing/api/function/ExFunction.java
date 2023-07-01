package io.github.athingx.athing.thing.api.function;

public interface ExFunction<T, R, X extends Throwable> {

    R apply(T t) throws X;

}
