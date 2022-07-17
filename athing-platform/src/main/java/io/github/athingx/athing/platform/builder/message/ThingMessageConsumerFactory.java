package io.github.athingx.athing.platform.builder.message;

import jakarta.jms.JMSException;

/**
 * 设备消息消费者工厂
 */
public interface ThingMessageConsumerFactory {

    /**
     * 生产设备消息消费者
     *
     * @return 设备消息消费者
     * @throws JMSException 生产失败
     */
    ThingMessageConsumer make() throws JMSException;

}
