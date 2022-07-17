package io.github.athingx.athing.platform.builder.client;

import com.aliyuncs.v5.IAcsClient;

/**
 * 设备客户端工厂
 */
public interface IAcsClientFactory {

    /**
     * 生产设备客户端
     *
     * @return 设备客户端
     * @throws Exception 生产失败
     */
    IAcsClient make() throws Exception;

}
