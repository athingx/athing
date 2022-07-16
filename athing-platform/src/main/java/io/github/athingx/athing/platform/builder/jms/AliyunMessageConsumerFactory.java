package io.github.athingx.athing.platform.builder.jms;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import org.apache.qpid.jms.JmsQueue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static io.github.athingx.athing.platform.impl.util.IOUtils.closeQuietly;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class AliyunMessageConsumerFactory implements MessageConsumerFactory {

    private String identity;
    private String secret;
    private String remote;
    private String group;
    private ConnectionFactory connectionFactory;

    public AliyunMessageConsumerFactory identity(String identity) {
        this.identity = identity;
        return this;
    }

    public AliyunMessageConsumerFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    public AliyunMessageConsumerFactory remote(String remote) {
        this.remote = remote;
        return this;
    }

    public AliyunMessageConsumerFactory group(String group) {
        this.group = group;
        return this;
    }

    public AliyunMessageConsumerFactory identity(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    @Override
    public MessageConsumer make() throws JMSException {

        requireNonNull(identity, "identity is required!");
        requireNonNull(secret, "secret is required!");
        requireNonNull(remote, "remote is required!");
        requireNonNull(group, "group is required!");

        final String uniqueId = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();
        connectionFactory = Objects.isNull(connectionFactory) ? new AliyunConnectionFactory() : connectionFactory;
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.make(
                    remote,
                    getUsername(identity, timestamp, uniqueId, group),
                    getPassword(identity, secret, timestamp)
            );
            session = connection.createSession(Session.CLIENT_ACKNOWLEDGE);
            final var _connection = connection;
            final var _session = session;
            final var consumer = session.createConsumer(new JmsQueue(group));
            return (MessageConsumer) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{MessageConsumer.class}, new InvocationHandler() {

                private boolean isCloseMethod(Method method) {
                    return Modifier.isPublic(method.getModifiers())
                            && method.getParameterCount() == 0
                            && method.getName().equals("close");
                }

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    final Object returnObj = method.invoke(consumer, args);

                    // 这里做个桥接，当消息消费者被关闭的时候，同时也释放会话和连接资源
                    if (isCloseMethod(method)) {
                        closeQuietly(_session);
                        closeQuietly(_connection);
                    }

                    return returnObj;
                }

            });
        } catch (Throwable cause) {
            closeQuietly(session);
            closeQuietly(connection);
            if (cause instanceof JMSException jmsCause) {
                throw jmsCause;
            } else {
                throw new RuntimeException("init jms error!", cause);
            }
        }

    }

    // 计算并获取账号
    private static String getUsername(final String identity,
                                      final long timestamp,
                                      final String uniqueId,
                                      final String group) {
        return "%s|authMode=aksign,signMethod=hmacsha1,timestamp=%s,authId=%s,consumerGroupId=%s|".formatted(
                uniqueId,
                timestamp,
                identity,
                group
        );
    }

    // 计算并获取密码
    private static String getPassword(final String identity,
                                      final String secret,
                                      final long timestamp) {
        final String content = "authId=%s&timestamp=%s".formatted(identity, timestamp);
        try {
            final Mac mac = Mac.getInstance("HMACSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(UTF_8), mac.getAlgorithm()));
            return Base64.getEncoder().encodeToString(mac.doFinal(content.getBytes(UTF_8)));
        } catch (Exception cause) {
            throw new IllegalStateException(cause);
        }
    }

}
