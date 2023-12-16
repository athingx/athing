package io.github.athingx.athing.thing.api;

import io.github.athingx.athing.thing.api.op.ThingOp;
import io.github.athingx.athing.thing.api.plugin.ThingPlugins;

import java.util.concurrent.Executor;

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
    ThingOp<byte[], byte[]> op();

    /**
     * 获取线程池
     *
     * @return 线程池
     */
    Executor executor();

    /**
     * 设备插件管理器
     *
     * @return 设备插件管理器
     */
    ThingPlugins plugins();

    /**
     * 判断设备是否已被销毁
     *
     * @return TRUE | FALSE
     */
    boolean isDestroyed();

    /**
     * 判断设备是否已连接
     *
     * @return TRUE | FALSE
     */
    boolean isConnected();

    /**
     * 销毁设备
     */
    void destroy();

}
