package io.github.athingx.athing.platform.builder;

import io.github.athingx.athing.platform.api.ThingPlatform;
import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import io.github.athingx.athing.platform.builder.iot.IotClientFactory;
import io.github.athingx.athing.platform.builder.jms.MessageConsumerFactory;
import io.github.athingx.athing.platform.impl.ThingPlatformImpl;
import io.github.athingx.athing.platform.impl.message.ThingMessageConsumer;
import jakarta.jms.JMSException;

/**
 * 设备平台构造器
 */
public class ThingPlatformBuilder {

    private IotClientFactory iotFactory;
    private ThingMessageConsumerFactory jmsFactory;

    /**
     * 设置设备消息消费
     *
     * @param mcFactory JMS消息消费者工厂
     * @param listener  设备消息监听器
     * @return this
     */
    public ThingPlatformBuilder consumer(MessageConsumerFactory mcFactory, ThingMessageListener listener) {
        this.jmsFactory = () -> new ThingMessageConsumer(mcFactory.make(), listener);
        return this;
    }

    /**
     * 设置设备平台客户端
     *
     * @param tpcFactory 设备平台客户端工厂
     * @return this
     */
    public ThingPlatformBuilder client(IotClientFactory tpcFactory) {
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
                jmsFactory.make()
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
