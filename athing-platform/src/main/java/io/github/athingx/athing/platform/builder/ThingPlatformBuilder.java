package io.github.athingx.athing.platform.builder;

import io.github.athingx.athing.platform.api.ThingPlatform;
import io.github.athingx.athing.platform.builder.client.IAcsClientFactory;
import io.github.athingx.athing.platform.builder.message.ThingMessageConsumerFactory;
import io.github.athingx.athing.platform.impl.ThingPlatformImpl;

/**
 * 设备平台构造器
 */
public class ThingPlatformBuilder {

    private IAcsClientFactory iotFactory = () -> null;
    private ThingMessageConsumerFactory tmcFactory = () -> null;

    /**
     * 设备消息消费工厂
     *
     * @param tmcFactory 设备消息消费工厂
     * @return this
     */
    public ThingPlatformBuilder consumerFactory(ThingMessageConsumerFactory tmcFactory) {
        this.tmcFactory = tmcFactory;
        return this;
    }

    /**
     * 设置设备平台客户端
     *
     * @param tpcFactory 设备平台客户端工厂
     * @return this
     */
    public ThingPlatformBuilder clientFactory(IAcsClientFactory tpcFactory) {
        this.iotFactory = tpcFactory;
        return this;
    }

    /**
     * 构造设备平台
     *
     * @return 设备平台
     * @throws Exception 构造失败
     */
    public ThingPlatform build() throws Exception {
        return new ThingPlatformImpl(
                iotFactory.make(),
                tmcFactory.make()
        );
    }

}
