package io.github.athingx.athing.platform.impl;

import io.github.athingx.athing.platform.api.ThingPlatform;
import io.github.athingx.athing.platform.api.ThingTemplate;
import io.github.athingx.athing.platform.api.ThingTemplateFactory;
import io.github.athingx.athing.platform.api.client.ThingClient;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;
import io.github.athingx.athing.platform.builder.message.ThingMessageConsumer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.athingx.athing.platform.impl.util.IOUtils.closeQuietly;

/**
 * 设备平台实现
 */
public class ThingPlatformImpl implements ThingPlatform {

    private final ThingClient client;
    private final ThingMessageConsumer consumer;
    private final Map<Class<?>, ThingTemplateFactory<?>> templateFactoryMap = new ConcurrentHashMap<>();

    /**
     * 设备平台实现
     *
     * @param client   设备平台客户端
     * @param consumer 设备消息消费者
     */
    public ThingPlatformImpl(ThingClient client, ThingMessageConsumer consumer) {
        this.client = client;
        this.consumer = consumer;
    }

    // 获取设备模板工厂
    @SuppressWarnings("unchecked")
    private <T extends ThingTemplate> ThingTemplateFactory<T> getFactory(Class<T> type) {
        return (ThingTemplateFactory<T>) templateFactoryMap.getOrDefault(type, (client, productId, thingId) -> null);
    }

    private void checkSupportThingMessage() {
        if (Objects.isNull(consumer)) {
            throw new UnsupportedOperationException("not support thing-message");
        }
    }

    private void checkSupportThingTemplate() {
        if (Objects.isNull(client)) {
            throw new UnsupportedOperationException("not support thing-template");
        }
    }

    @Override
    public <T extends ThingTemplate> T genThingTemplate(Class<T> type, String productId, String thingId) {
        return getFactory(type).make(client, productId, thingId);
    }

    @Override
    public void register(ThingMessageDecoder<?> decoder) {
        checkSupportThingMessage();
        consumer.appendDecoder(decoder);
    }

    @Override
    public <T extends ThingTemplate> void register(Class<T> type, ThingTemplateFactory<T> factory) {
        checkSupportThingTemplate();
        if (null != templateFactoryMap.putIfAbsent(type, Objects.requireNonNull(factory))) {
            throw new IllegalArgumentException("duplicate type: %s".formatted(type.getName()));
        }
    }

    @Override
    public <T extends ThingTemplate> void register(Class<T> type, ThingTemplateFactory<T> factory, ThingMessageDecoder<?> decoder) {
        checkSupportThingMessage();
        checkSupportThingTemplate();
        register(type, factory);
        register(decoder);
    }

    @Override
    public void close() {
        closeQuietly(consumer);
    }

}
