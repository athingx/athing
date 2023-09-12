package io.github.athingx.athing.thing.api.plugin;

import io.github.athingx.athing.thing.api.Thing;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * 设备插件安装器
 *
 * @param <T> 插件类型
 */
public interface ThingPluginInstaller<T extends ThingPlugin> {

    /**
     * 获取插件元数据
     *
     * @return 插件元数据
     */
    Meta<T> meta();

    /**
     * 安装插件
     *
     * @param thing 设备
     * @return 安装结果
     */
    CompletableFuture<T> install(Thing thing);

    /**
     * 插件元数据
     *
     * @param name       插件名称
     * @param type       插件类型
     * @param properties 插件属性
     */
    record Meta<T>(String name, Class<T> type, Properties properties) {

        /**
         * 构造插件元数据
         *
         * @param name 插件名称
         * @param type 插件类型
         */
        public Meta(String name, Class<T> type) {
            this(name, type, new Properties());
        }

    }

}
