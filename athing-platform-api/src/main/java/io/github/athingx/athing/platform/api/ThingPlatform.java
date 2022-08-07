package io.github.athingx.athing.platform.api;

import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;

/**
 * 设备平台
 */
public interface ThingPlatform {

    /**
     * 生成设备模版
     *
     * @param type      模板类型
     * @param productId 产品ID
     * @param thingId   设备ID
     * @return 设备模版
     */
    <T extends ThingTemplate> T genThingTemplate(Class<T> type, String productId, String thingId);

    /**
     * 注册消息解码器
     *
     * @param decoder 消息解码器
     */
    void register(ThingMessageDecoder<?> decoder);

    /**
     * 注册设备模板
     *
     * @param type    模板类型
     * @param factory 模板工厂
     * @param <T>     模板类型
     */
    <T extends ThingTemplate> void register(Class<T> type, ThingTemplateFactory<T> factory);

    /**
     * 注册设备模板
     *
     * @param type    模板类型
     * @param factory 模板工厂
     * @param decoder 消息解码器
     * @param <T>     模板类型
     */
    <T extends ThingTemplate> void register(Class<T> type, ThingTemplateFactory<T> factory, ThingMessageDecoder<?> decoder);

    /**
     * 销毁设备平台
     */
    void destroy();

}
