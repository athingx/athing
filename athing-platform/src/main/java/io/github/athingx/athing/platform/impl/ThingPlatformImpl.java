package io.github.athingx.athing.platform.impl;

import com.aliyuncs.IAcsClient;
import io.github.athingx.athing.platform.api.ThingPlatform;
import io.github.athingx.athing.platform.api.ThingTemplate;
import io.github.athingx.athing.platform.api.ThingTemplateFactory;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;
import io.github.athingx.athing.platform.impl.message.ThingMessageConsumer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备平台实现
 */
public class ThingPlatformImpl implements ThingPlatform {

    private final IAcsClient client;
    private final ThingMessageConsumer consumer;
    private final Map<Class<?>, ThingTemplateFactory<?>> templateFactoryMap = new ConcurrentHashMap<>();

    /**
     * 设备平台实现
     *
     * @param client   设备平台客户端
     * @param consumer 设备消息消费者
     */
    public ThingPlatformImpl(IAcsClient client, ThingMessageConsumer consumer) {
        this.client = client;
        this.consumer = consumer;
    }

    // 获取设备模板工厂
    @SuppressWarnings("unchecked")
    private <T extends ThingTemplate> ThingTemplateFactory<T> getFactory(Class<T> type) {
        return (ThingTemplateFactory<T>) templateFactoryMap.getOrDefault(type, (client, productId, thingId) -> null);
    }

    @Override
    public <T extends ThingTemplate> T genThingTemplate(Class<T> type, String productId, String thingId) {
        return getFactory(type).make(client, productId, thingId);
    }

    @Override
    public void register(ThingMessageDecoder<?> decoder) {
        consumer.decoders().add(decoder);
    }

    @Override
    public <T extends ThingTemplate> void register(Class<T> type, ThingTemplateFactory<T> factory, ThingMessageDecoder<?> decoder) {

        // 注册&判重
        if (null != templateFactoryMap.putIfAbsent(type, Objects.requireNonNull(factory))) {
            throw new IllegalArgumentException("duplicate type: %s".formatted(type.getName()));
        }

        // 注册消息解码器
        register(decoder);

    }

    @Override
    public void close() throws Exception {
        client.shutdown();
        consumer.close();
    }

}
