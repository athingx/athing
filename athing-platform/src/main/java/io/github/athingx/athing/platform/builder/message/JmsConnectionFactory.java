package io.github.athingx.athing.platform.builder.message;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

/**
 * JMS消息连接工厂
 */
public interface JmsConnectionFactory {

    /**
     * 生产连接
     *
     * @return JMS消息连接
     * @throws JMSException 生产失败
     */
    Connection make() throws JMSException;

}
