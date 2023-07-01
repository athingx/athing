package io.github.athingx.athing.thing.api.function;

public interface ExSupplier<T, X extends Throwable> {

    T get() throws X;

}
