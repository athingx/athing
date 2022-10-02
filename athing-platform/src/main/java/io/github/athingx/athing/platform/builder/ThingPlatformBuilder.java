package io.github.athingx.athing.platform.builder;

import com.aliyuncs.v5.IAcsClient;
import io.github.athingx.athing.platform.api.ThingPlatform;
import io.github.athingx.athing.platform.builder.client.IAcsClientFactory;
import io.github.athingx.athing.platform.builder.message.ThingMessageConsumerFactory;
import io.github.athingx.athing.platform.impl.ThingPlatformImpl;
import io.github.athingx.athing.platform.message.ThingMessageConsumer;

/**
 * 设备平台构造器
 */
public class ThingPlatformBuilder {

    private IAcsClientFactory iacFactory = () -> null;
    private ThingMessageConsumerFactory tmcFactory = () -> null;

    /**
     * 设置设备消息消费者工厂
     *
     * @param tmcFactory 设备消息消费者工厂
     * @return this
     */
    public ThingPlatformBuilder consumerFactory(ThingMessageConsumerFactory tmcFactory) {
        this.tmcFactory = tmcFactory;
        return this;
    }

    /**
     * 设置设备消息消费者
     * @param consumer 设备消息消费者
     * @return this
     */
    public ThingPlatformBuilder consumer(ThingMessageConsumer consumer) {
        return consumerFactory(() -> consumer);
    }

    /**
     * 设置设备平台客户端工厂
     *
     * @param tpcFactory 设备平台客户端工厂
     * @return this
     */
    public ThingPlatformBuilder clientFactory(IAcsClientFactory tpcFactory) {
        this.iacFactory = tpcFactory;
        return this;
    }

    /**
     * 设置设备平台客户端
     * @param client 设备平台客户端
     * @return this
     */
    public ThingPlatformBuilder client(IAcsClient client) {
        return clientFactory(()->client);
    }

    /**
     * 构造设备平台
     *
     * @return 设备平台
     * @throws Exception 构造失败
     */
    public ThingPlatform build() throws Exception {
        return new ThingPlatformImpl(
                iacFactory.make(),
                tmcFactory.make()
        );
    }

}
