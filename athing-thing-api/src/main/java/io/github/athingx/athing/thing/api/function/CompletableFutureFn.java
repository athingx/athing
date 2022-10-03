package io.github.athingx.athing.thing.api.function;

import io.github.athingx.athing.thing.api.op.OpReply;
import io.github.athingx.athing.thing.api.op.OpReplyException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@link CompletableFuture}函数集合
 */
public class CompletableFutureFn {

    /**
     * 可执行
     *
     * @param <T> future数据类型
     */
    @FunctionalInterface
    public interface Executable<T> {

        /**
         * 执行
         *
         * @param future future
         * @throws Throwable 执行失败
         */
        void execute(CompletableFuture<T> future) throws Throwable;

    }

    /**
     * 执行，如执行抛出异常，将会让目标future直接失败
     *
     * @param fn  执行函数
     * @param <T> future数据类型
     * @return future
     */
    public static <T> CompletableFuture<T> tryCatchExecute(Executable<T> fn) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        try {
            fn.execute(future);
        } catch (Throwable cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    /**
     * 可完成
     *
     * @param <T> future数据类型
     */
    @FunctionalInterface
    public interface Completable<T> {

        /**
         * 完成
         *
         * @return 完成数据
         * @throws Throwable 完成失败
         */
        T complete() throws Throwable;

    }

    /**
     * 完成，如执完成出异常，将会让目标future直接失败
     *
     * @param fn  完成函数
     * @param <T> future数据类型
     * @return future
     */
    public static <T> CompletableFuture<T> tryCatchComplete(Completable<T> fn) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        try {
            future.complete(fn.complete());
        } catch (Throwable cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    /**
     * 用于{@link CompletableFuture#whenComplete(BiConsumer)}
     *
     * @param successFn 成功函数
     * @param failureFn 失败函数
     * @param <T>       future数据类型
     * @return function for {@link CompletableFuture#whenComplete(BiConsumer)}
     */
    public static <T> BiConsumer<T, Throwable> whenCompleted(Consumer<T> successFn, Consumer<Throwable> failureFn) {
        return (t, cause) -> {
            if (Objects.isNull(cause)) {
                successFn.accept(t);
            } else {
                failureFn.accept(cause);
            }
        };
    }

    /**
     * 用于{@link CompletableFuture#whenComplete(BiConsumer)}
     *
     * @param predicate 过滤函数
     * @param successFn 成功函数
     * @param failureFn 失败函数
     * @param <T>       future数据类型
     * @return function for {@link CompletableFuture#whenComplete(BiConsumer)}
     */
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

    /**
     * 用于{@link CompletableFuture#whenComplete(BiConsumer)}
     *
     * @param failureFn 失败函数
     * @param <T>       future数据类型
     * @return function for {@link CompletableFuture#whenComplete(BiConsumer)}
     */
    public static <T> BiConsumer<T, Throwable> whenExceptionally(Consumer<Throwable> failureFn) {
        return whenCompleted(emptyFn(), failureFn);
    }

    /**
     * 用于{@link CompletableFuture#whenComplete(BiConsumer)}
     *
     * @param successFn 成功函数
     * @param <T>       future数据类型
     * @return function for {@link CompletableFuture#whenComplete(BiConsumer)}
     */
    public static <T> BiConsumer<T, Throwable> whenSuccessfully(Consumer<T> successFn) {
        return whenCompleted(successFn, emptyFn());
    }

    /**
     * 用于处理结果为{@link OpReply}的成功
     * <ul>
     *     <li>如果应答结果成功，则返回结果；</li>
     *     <li>如果应答结果失败，则抛出{@link OpReplyException}异常</li>
     * </ul>
     *
     * @param <T> 应答结果类型
     * @return function for {@link CompletableFuture#thenCompose(Function)}
     */
    public static <T> Function<OpReply<T>, CompletionStage<T>> thenComposeOpReply() {
        return reply -> tryCatchComplete(() -> {

            // 如果应答返回错误的编码，则抛出应答异常
            if (!reply.isOk()) {
                throw new OpReplyException(
                        reply.token(),
                        reply.code(),
                        reply.desc()
                );
            }

            // 应答返回成功结果，返回应答数据结果
            return reply.data();

        });
    }

}
