package io.github.athingx.athing.platform.builder.message;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

/**
 * 阿里云JMS连接工厂
 */
public class AliyunJmsConnectionFactory implements JmsConnectionFactory {

    @Override
    public Connection make(String remote, String username, String password) throws JMSException {
        final Connection connection = new org.apache.qpid.jms.JmsConnectionFactory(username, password, remote)
                .createConnection();
        connection.start();
        return connection;
    }

}
