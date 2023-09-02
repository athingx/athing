package io.github.athingx.athing.thing.api.plugin;

import io.github.athingx.athing.thing.api.Thing;

import java.util.concurrent.CompletableFuture;

/**
 * 设备插件安装器
 *
 * @param <T> 插件类型
 */
public interface ThingPluginInstaller<T extends ThingPlugin> {

    /**
     * 安装插件
     *
     * @param thing 设备
     * @return 安装结果
     */
    CompletableFuture<ThingPlugin> install(Thing thing);

}
