package io.github.athingx.athing.platform.builder.iot;

import com.aliyun.iot20180120.Client;

/**
 * 设备平台客户端工厂
 */
public interface IotClientFactory {

    /**
     * 生产设备平台客户端
     *
     * @return 设备平台客户端
     * @throws Exception 生产失败
     */
    Client make() throws Exception;

}
