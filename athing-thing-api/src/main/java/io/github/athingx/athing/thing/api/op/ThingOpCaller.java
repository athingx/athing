package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

public interface ThingOpCaller<T, R> extends ThingOpBinder {

    CompletableFuture<R> call(T data);

}
