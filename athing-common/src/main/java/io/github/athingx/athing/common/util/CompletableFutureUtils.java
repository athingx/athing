package io.github.athingx.athing.common.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CompletableFutureUtils {

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        final var future = new CompletableFuture<T>();
        try {
            future.complete(supplier.get());
        } catch (Throwable ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

}
