package io.github.athingx.athing.thing.api.function;

public interface ExRunnable<X extends Throwable> {

    void run() throws X;

}
