package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpSupplier;

import java.util.concurrent.CompletableFuture;

/**
 * 数据投递者
 *
 * @param <V> 投递数据类型
 */
public interface ThingOpPoster<V> extends ThingOpBinder {

    /**
     * 投递数据
     *
     * @param supplier 获取数据函数
     * @return 投递结果
     */
    CompletableFuture<V> post(OpSupplier<V> supplier);

    /**
     * 投递数据
     *
     * @param data 数据
     * @return 投递结果
     */
    default CompletableFuture<V> post(V data) {
        return post(((topic, token) -> data));
    }

}
