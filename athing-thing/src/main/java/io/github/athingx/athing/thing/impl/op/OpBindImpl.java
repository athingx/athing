package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.thing.api.op.OpBind;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

/**
 * 操作绑定实现
 *
 * @param <T> 绑定源头类型
 * @param <V> 绑定目标类型
 */
abstract class OpBindImpl<T, V> implements OpBind<V> {

    /**
     * {@link #mapper()}的短路逻辑：逻辑假
     */
    private final static CompletableFuture<Boolean> FALSE_CF = completedFuture(false);

    /**
     * {@link #mapper()}的短路逻辑：逻辑真
     */
    private final static CompletableFuture<Boolean> TRUE_CF = completedFuture(true);

    /**
     * {@link #mapper()}的短路逻辑：短路流程控制异常
     */
    private final static SkipException SKIP_EX = new SkipException();

    /**
     * {@link #mapper()}的短路逻辑
     */
    private BiFunction<String, V, CompletableFuture<Boolean>> matcher = (topic, data) -> TRUE_CF;

    /**
     * 数据映射函数
     */
    private final BiFunction<String, ? super T, CompletableFuture<V>> mapper;

    /**
     * 操作绑定实现
     *
     * @param mapper 映射函数
     */
    public OpBindImpl(BiFunction<String, ? super T, CompletableFuture<V>> mapper) {
        this.mapper = mapper;
    }

    @Override
    public OpBind<V> matchesAsync(BiFunction<String, ? super V, CompletableFuture<Boolean>> fn) {
        this.matcher = (topic, data) -> matcher
                .apply(topic, data)
                .thenCompose(test -> test
                        ? fn.apply(topic, data)
                        : FALSE_CF
                );
        return this;
    }

    @Override
    public <R> OpBind<R> mapAsync(BiFunction<String, ? super V, CompletableFuture<R>> fn) {
        return newOpBind((topic, before) -> mapper
                .apply(topic, before)
                .thenCompose(data -> matcher
                        .apply(topic, data)
                        .thenCompose(test -> test
                                ? fn.apply(topic, data)
                                : failedFuture(SKIP_EX)
                        )));
    }

    /**
     * 构建操作绑定
     *
     * @param mapper 绑定映射函数
     * @param <R>    绑定目标类型
     * @return 操作绑定实现
     */
    abstract <R> OpBindImpl<T, R> newOpBind(BiFunction<String, ? super T, CompletableFuture<R>> mapper);

    /**
     * 判断异常是否为非跳过控制异常
     *
     * @param ex 异常
     * @return TRUE: 非跳过异常 ; FALSE: 是跳过异常
     */
    boolean isNotSkipEx(Throwable ex) {
        return ex != SKIP_EX;
    }

    /**
     * 数据映射函数，该函数将会为设备绑定完成数据映射
     *
     * @return 数据映射函数
     */
    BiFunction<String, ? super T, CompletableFuture<V>> mapper() {
        return mapper;
    }

    /**
     * 跳过异常，该异常用仅用于{@link #mapper()}的短路逻辑
     */
    private static class SkipException extends RuntimeException {
        @Override
        public Throwable fillInStackTrace() {
            return null;
        }
    }

}
