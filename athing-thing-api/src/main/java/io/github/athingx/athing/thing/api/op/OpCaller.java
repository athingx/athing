package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

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

}
