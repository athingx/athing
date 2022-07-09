package io.github.athingx.athing.platform.builder;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;

/**
 * 设备平台客户端工厂
 */
public interface AcsClientFactory {

    /**
     * 生产设备平台客户端
     *
     * @return 设备平台客户端
     * @throws ClientException 生产失败
     */
    IAcsClient make() throws ClientException;

}
