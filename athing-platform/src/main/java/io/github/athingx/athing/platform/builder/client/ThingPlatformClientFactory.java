package io.github.athingx.athing.platform.builder.client;

import io.github.athingx.athing.platform.api.client.ThingPlatformClient;

/**
 * 设备平台客户端工厂
 */
public interface ThingPlatformClientFactory {

    /**
     * 生产设备平台客户端
     *
     * @return 设备平台客户端
     * @throws Exception 生产失败
     */
    ThingPlatformClient make() throws Exception;

}
