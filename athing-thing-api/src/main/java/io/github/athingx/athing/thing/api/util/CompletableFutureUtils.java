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
     * 为指定的{@link CompletableFuture}执行一段代码
     * <ul>
     *     <li>如果代码执行失败，则{@link CompletableFuture}标记为异常</li>
     *     <li>如果代码执行成功，则{@link CompletableFuture}原样返回</li>
     * </ul>
     *
     * @param future {@link CompletableFuture}
     * @param fn     执行代码
     * @param <T>    {@link CompletableFuture}数据类型
     * @param <F>    {@link CompletableFuture}类型
     * @return {@link CompletableFuture}
     */
    public static <T, F extends CompletableFuture<T>> F executeFuture(F future, ExRunnable<Throwable> fn) {
        try {
            fn.run();
        } catch (Throwable cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    /**
     * 创建{@link CompletableFuture}并执行一段代码
     * <ul>
     *     <li>如果代码执行失败，则{@link CompletableFuture}标记为异常</li>
     *     <li>如果代码执行成功，则{@link CompletableFuture}原样返回</li>
     * </ul>
     *
     * @param fn  执行代码
     * @param <T> {@link CompletableFuture}数据类型
     * @return {@link CompletableFuture}
     */
    public static <T> CompletableFuture<T> executeFuture(ExRunnable<Throwable> fn) {
        return executeFuture(new CompletableFuture<>(), fn);
    }

    /**
     * @see #executeFuture(CompletableFuture, ExRunnable)
     */
    public static <T, F extends CompletableFuture<T>> F executeFuture(F future, ExConsumer<F, Throwable> fn) {
        try {
            fn.accept(future);
        } catch (Throwable cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    /**
     * @see #executeFuture(ExRunnable)
     */
    public static <T> CompletableFuture<T> executeFuture(ExConsumer<CompletableFuture<T>, Throwable> fn) {
        return executeFuture(new CompletableFuture<>(), fn);
    }

    /**
     * 获取值并完成指定的{@link CompletableFuture}
     * <ul>
     *     <li>如果取值失败，则{@link CompletableFuture}标记为异常</li>
     *     <li>如果取值成功，则{@link CompletableFuture}标记为完成，并赋予对应的值</li>
     * </ul>
     *
     * @param future {@link CompletableFuture}
     * @param fn     获取值
     * @param <T>    {@link CompletableFuture}数据类型
     * @param <F>    {@link CompletableFuture}类型
     * @return {@link CompletableFuture}
     */
    public static <T, F extends CompletableFuture<T>> F completedFuture(F future, ExSupplier<T, Throwable> fn) {
        try {
            future.complete(fn.get());
        } catch (Throwable cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    /**
     * 获取值并创建{@link CompletableFuture}
     * <ul>
     *     <li>如果取值失败，则{@link CompletableFuture}标记为异常</li>
     *     <li>如果取值成功，则{@link CompletableFuture}标记为完成，并赋予对应的值</li>
     * </ul>
     *
     * @param fn  获取值
     * @param <T> {@link CompletableFuture}数据类型
     * @return {@link CompletableFuture}
     */
    public static <T> CompletableFuture<T> completedFuture(ExSupplier<T, Throwable> fn) {
        return completedFuture(new CompletableFuture<>(), fn);
    }

    /**
     * 创建{@link CompletableFuture}并执行一段代码后标记为完成
     * <ul>
     *     <li>如果取值失败，则{@link CompletableFuture}标记为异常</li>
     *     <li>如果取值成功，则{@link CompletableFuture}标记为完成</li>
     * </ul>
     *
     * @param fn 执行代码
     * @return {@link CompletableFuture}
     */
    public static CompletableFuture<Void> completedFuture(ExRunnable<Throwable> fn) {
        return completedFuture(() -> {
            fn.run();
            return null;
        });
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
        return reply -> reply.isOk()
                ? CompletableFuture.completedFuture(reply.data())
                : CompletableFuture.failedFuture(new OpReplyException(
                reply.token(),
                reply.code(),
                reply.desc()
        ));
    }

    public interface ExConsumer<T, X extends Throwable> {

        void accept(T t) throws X;

    }

    public interface ExFunction<T, R, X extends Throwable> {

        R apply(T t) throws X;

    }

    public interface ExRunnable<X extends Throwable> {

        void run() throws X;

    }

    public interface ExSupplier<T, X extends Throwable> {

        T get() throws X;

    }
}
