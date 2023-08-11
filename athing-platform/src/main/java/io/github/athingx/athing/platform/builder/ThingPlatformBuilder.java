package io.github.athingx.athing.platform.builder;

import io.github.athingx.athing.platform.api.ThingPlatform;
import io.github.athingx.athing.platform.api.client.ThingPlatformClient;
import io.github.athingx.athing.platform.builder.client.ThingPlatformClientFactory;
import io.github.athingx.athing.platform.builder.message.ThingMessageConsumerFactory;
import io.github.athingx.athing.platform.impl.ThingPlatformImpl;
import io.github.athingx.athing.platform.message.ThingMessageConsumer;

/**
 * 设备平台构造器
 */
public class ThingPlatformBuilder {

    private ThingPlatformClientFactory tpcFactory = () -> null;
    private ThingMessageConsumerFactory tmcFactory = () -> null;

    /**
     * 设置设备消息消费者工厂
     *
     * @param tmcFactory 设备消息消费者工厂
     * @return this
     */
    public ThingPlatformBuilder consumer(ThingMessageConsumerFactory tmcFactory) {
        this.tmcFactory = tmcFactory;
        return this;
    }

    public ThingPlatformBuilder consumer(ThingMessageConsumer consumer) {
        return consumer(() -> consumer);
    }

    /**
     * 设置设备平台客户端工厂
     *
     * @param tpcFactory 设备平台客户端工厂
     * @return this
     */
    public ThingPlatformBuilder client(ThingPlatformClientFactory tpcFactory) {
        this.tpcFactory = tpcFactory;
        return this;
    }

    public ThingPlatformBuilder client(ThingPlatformClient client) {
        return client(() -> client);
    }

    /**
     * 构造设备平台
     *
     * @return 设备平台
     * @throws Exception 构造失败
     */
    public ThingPlatform build() throws Exception {
        return new ThingPlatformImpl(
                tpcFactory.make(),
                tmcFactory.make()
        );
    }

}
