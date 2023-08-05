package io.github.athingx.athing.thing.api.util;

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
public class CompletableFutureUtils {

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
        return reply -> reply.isSuccess()
                ? CompletableFuture.completedFuture(reply.data())
                : CompletableFuture.failedFuture(new OpReplyException(reply.token(), reply.code(), reply.desc()));
    }

}
