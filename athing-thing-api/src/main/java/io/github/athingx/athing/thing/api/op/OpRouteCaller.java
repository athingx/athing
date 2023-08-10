package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

public interface OpRouteCaller<T, R> extends OpBinder {

    CompletableFuture<R> call(T data);

}
