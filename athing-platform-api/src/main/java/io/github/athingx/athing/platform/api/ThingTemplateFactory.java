package io.github.athingx.athing.platform.api;

import com.aliyuncs.v5.IAcsClient;

/**
 * 设备模板工厂
 *
 * @param <T> 模板类型
 */
public interface ThingTemplateFactory<T extends ThingTemplate> {

    /**
     * 生产设备模板
     *
     * @param client    设备平台客户端
     * @param productId 产品ID
     * @param thingId   设备ID
     * @return 设备模板
     */
    T make(IAcsClient client, String productId, String thingId);


}
