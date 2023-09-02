package io.github.athingx.athing.thing.api.plugin;

import java.util.concurrent.CompletableFuture;

/**
 * 设备插件
 */
public interface ThingPlugin {

    /**
     * 卸载插件
     *
     * @return 卸载结果
     */
    CompletableFuture<Void> uninstall();

}
