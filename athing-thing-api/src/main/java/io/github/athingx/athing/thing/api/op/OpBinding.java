package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.Thing;

import java.util.concurrent.CompletableFuture;

/**
 * 绑定设备操作
 *
 * @param <T>
 */
public interface OpBinding<T extends OpBinder> {

    /**
     * 绑定设备操作
     *
     * @param thing 设备
     * @return 设备操作
     */
    CompletableFuture<T> bind(Thing thing);

}
