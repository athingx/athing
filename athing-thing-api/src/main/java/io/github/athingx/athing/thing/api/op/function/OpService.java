package io.github.athingx.athing.thing.api.op.function;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface OpService<T, R> {

    CompletableFuture<R> service(String topic, T data);

}
