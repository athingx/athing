package io.github.athingx.athing.platform.builder.jms;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

public interface ConnectionFactory {

    Connection make(String remote, String username, String password) throws JMSException;

}
