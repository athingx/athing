package io.github.athingx.athing.thing.api.util;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class CompletableFutureUtils {

    @FunctionalInterface
    public interface Executable<T> {

        void execute(CompletableFuture<T> future) throws Throwable;

    }

    public static <T> CompletableFuture<T> tryCatchExecute(Executable<T> fn) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        try {
            fn.execute(future);
        } catch (Throwable cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    @FunctionalInterface
    public interface Completed<T> {

        T complete() throws Throwable;

    }

    public static <T> CompletableFuture<T> tryCatchCompleted(Completed<T> fn) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        try {
            future.complete(fn.complete());
        } catch (Throwable cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    public static <T> BiConsumer<T, Throwable> whenCompleted(Consumer<T> successFn, Consumer<Throwable> failureFn) {
        return (t, cause) -> {
            if (Objects.isNull(cause)) {
                successFn.accept(t);
            } else {
                failureFn.accept(cause);
            }
        };
    }

    public static <T> BiConsumer<T, Throwable> whenCompleted(BiPredicate<T, Throwable> predicate, Consumer<T> successFn, Consumer<Throwable> failureFn) {
        return (t, cause) -> {
            if (!predicate.test(t, cause)) {
                return;
            }
            if (Objects.isNull(cause)) {
                successFn.accept(t);
            } else {
                failureFn.accept(cause);
            }
        };
    }

    private static <T> Consumer<T> emptyFn() {
        return t -> {

        };
    }

    public static <T> BiConsumer<T, Throwable> whenExceptionally(Consumer<Throwable> failureFn) {
        return whenCompleted(emptyFn(), failureFn);
    }

    public static <T> BiConsumer<T, Throwable> whenSuccessfully(Consumer<T> successFn) {
        return whenCompleted(successFn, emptyFn());
    }

}
