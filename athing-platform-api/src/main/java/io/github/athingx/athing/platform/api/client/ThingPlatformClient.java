package io.github.athingx.athing.platform.api.client;

import io.github.athingx.athing.platform.api.ThingPlatformException;

/**
 * 设备平台客户端
 */
public interface ThingPlatformClient {

    /**
     * 执行请求
     *
     * @param request       请求
     * @param responseClass 响应类型
     * @param <T>           请求类型
     * @param <R>           响应类型
     * @return 响应
     * @throws ThingPlatformException 执行失败
     */
    <T, R> R execute(T request, Class<R> responseClass) throws ThingPlatformException;

    /**
     * 关闭平台客户端
     */
    void shutdown();

}
