package io.github.athingx.athing.platform.message;

import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 设备消息消费者
 */
public interface ThingMessageConsumer extends AutoCloseable {

    /**
     * 设置设备消息编码
     *
     * @param setupFn 设备消息编码设置函数
     */
    void decoders(Consumer<Set<ThingMessageDecoder<?>>> setupFn);

    /**
     * 获取设备消息编码集合
     *
     * @return 设备消息编码集合
     */
    Collection<ThingMessageDecoder<?>> decoders();

}
