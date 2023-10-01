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
     * @param identity   插件ID
     * @param type       插件类型
     * @param properties 插件属性
     */
    record Meta<T>(String identity, Class<T> type, Properties properties) {

        /**
         * 构造插件元数据
         *
         * @param identity 插件ID
         * @param type     插件类型
         */
        public Meta(String identity, Class<T> type) {
            this(identity, type, new Properties());
        }

    }

}
