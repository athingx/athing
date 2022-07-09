package io.github.athingx.athing.platform.builder;

import io.github.athingx.athing.platform.api.ThingPlatform;
import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import io.github.athingx.athing.platform.impl.ThingPlatformImpl;
import io.github.athingx.athing.platform.impl.message.ThingMessageConsumer;

import javax.jms.JMSException;

/**
 * 设备平台构造器
 */
public class ThingPlatformBuilder {

    private AcsClientFactory tpcFactory;
    private ThingMessageConsumerFactory tmcFactory;

    /**
     * 设置设备消息消费
     *
     * @param mcFactory JMS消息消费者工厂
     * @param listener  设备消息监听器
     * @return this
     */
    public ThingPlatformBuilder consumer(MessageConsumerFactory mcFactory, ThingMessageListener listener) {
        this.tmcFactory = () -> new ThingMessageConsumer(mcFactory.make(), listener);
        return this;
    }

    /**
     * 设置设备平台客户端
     *
     * @param tpcFactory 设备平台客户端工厂
     * @return this
     */
    public ThingPlatformBuilder client(AcsClientFactory tpcFactory) {
        this.tpcFactory = tpcFactory;
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
                tpcFactory.make(),
                tmcFactory.make()
        );
    }

    /**
     * 设备消息消费者工厂
     */
    private interface ThingMessageConsumerFactory {

        /**
         * 生产设备消息消费者
         *
         * @return 设备消息消费者
         * @throws JMSException 生产失败
         */
        ThingMessageConsumer make() throws JMSException;

    }
}
