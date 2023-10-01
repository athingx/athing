package io.github.athingx.athing.thing.api.plugin;

import java.util.concurrent.CompletableFuture;

/**
 * 设备插件管理器
 */
public interface ThingPlugins {

    /**
     * 安装插件
     *
     * @param installer 插件安装器
     * @param <T>       插件类型
     * @return 插件安装结果
     */
    <T extends ThingPlugin> CompletableFuture<T> install(ThingPluginInstaller<T> installer);

    /**
     * 依赖插件
     *
     * @param identity 插件ID
     * @param type     插件类型
     * @param <T>      插件类型
     * @return 插件依赖结果
     */
    <T extends ThingPlugin> CompletableFuture<T> depends(String identity, Class<T> type);

}
