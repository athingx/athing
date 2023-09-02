package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.Thing;

import java.util.concurrent.CompletableFuture;

/**
 * 设备操作绑定
 *
 * @param <T> 绑定类型
 */
public interface ThingOpBinding<T extends ThingOpBinder> {

    /**
     * 绑定设备操作
     *
     * @param thing 设备
     * @return 绑定结果
     */
    CompletableFuture<T> bind(Thing thing);

}
