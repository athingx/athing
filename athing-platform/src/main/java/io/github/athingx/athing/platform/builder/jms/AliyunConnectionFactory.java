package io.github.athingx.athing.platform.builder.jms;

import io.github.athingx.athing.platform.builder.jms.ConnectionFactory;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.apache.qpid.jms.JmsConnectionFactory;

public class AliyunConnectionFactory implements ConnectionFactory {

    @Override
    public Connection make(String remote, String username, String password) throws JMSException {
        return new JmsConnectionFactory(username, password, remote)
                .createConnection();
    }

}
