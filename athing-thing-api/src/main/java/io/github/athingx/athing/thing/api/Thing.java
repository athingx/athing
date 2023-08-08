package io.github.athingx.athing.thing.api;

import io.github.athingx.athing.thing.api.op.ThingOp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 设备
 */
public interface Thing {

    /**
     * 获取设备路径
     *
     * @return 设备路径
     */
    ThingPath path();

    /**
     * 获取设备操作
     *
     * @return 设备操作
     */
    ThingOp op();

    /**
     * 获取线程池
     *
     * @return 线程池
     */
    ExecutorService executor();

    /**
     * 销毁设备
     */
    void destroy();

}
