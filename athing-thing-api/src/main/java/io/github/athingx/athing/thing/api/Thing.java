package io.github.athingx.athing.thing.api;

import io.github.athingx.athing.thing.api.op.ThingOp;
import io.github.athingx.athing.thing.api.plugin.ThingPlugin;
import io.github.athingx.athing.thing.api.plugin.ThingPluginInstaller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    ThingOp op();

    /**
     * 获取线程池
     *
     * @return 线程池
     */
    Executor executor();

    /**
     * 安装设备插件
     *
     * @param installer 插件安装器
     * @param <T>       插件类型
     * @return 插件安装结果
     */
    <T extends ThingPlugin> CompletableFuture<T> install(ThingPluginInstaller<T> installer);

    <T extends ThingPlugin> Optional<CompletableFuture<T>> plugin(String name, Class<T> type);

    /**
     * 判断设备已被销毁
     *
     * @return TRUE | FALSE
     */
    boolean isDestroyed();

    /**
     * 销毁设备
     */
    void destroy();

}
