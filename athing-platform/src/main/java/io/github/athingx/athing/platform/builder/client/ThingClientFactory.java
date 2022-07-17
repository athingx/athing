package io.github.athingx.athing.platform.builder.client;

import io.github.athingx.athing.platform.api.client.ThingClient;

/**
 * 设备客户端工厂
 */
public interface ThingClientFactory {

    /**
     * 生产设备客户端
     *
     * @return 设备客户端
     * @throws Exception 生产失败
     */
    ThingClient make() throws Exception;

}
