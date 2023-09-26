package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.common.util.CompletableFutureUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 调用操作
 *
 * @param <T> 请求类型
 * @param <R> 应答类型
 */
public interface OpCaller<T, R> extends OpBinder {

    /**
     * 调用
     *
     * @param t 请求
     * @return 应答
     */
    CompletableFuture<R> call(T t);

    default <U> OpCaller<U, R> compose(Function<? super U, ? extends T> fn) {
        return new OpCaller<>() {
            @Override
            public CompletableFuture<R> call(U u) {
                return CompletableFutureUtils
                        .supply(() -> fn.apply(u))
                        .thenCompose(OpCaller.this::call);
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return OpCaller.this.unbind();
            }
        };
    }

    default <U> OpCaller<T, U> then(Function<? super R, ? extends U> fn) {
        return new OpCaller<>() {
            @Override
            public CompletableFuture<U> call(T t) {
                return OpCaller.this.call(t).thenApply(fn);
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return OpCaller.this.unbind();
            }
        };
    }

}
