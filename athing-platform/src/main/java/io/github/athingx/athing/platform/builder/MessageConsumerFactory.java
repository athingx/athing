package io.github.athingx.athing.platform.builder;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

/**
 * JMS消息消费者工厂
 */
public interface MessageConsumerFactory {

    /**
     * 生产JMS消息消费者
     *
     * @return JMS消息消费者
     * @throws JMSException 生产失败
     */
    MessageConsumer make() throws JMSException;

}
