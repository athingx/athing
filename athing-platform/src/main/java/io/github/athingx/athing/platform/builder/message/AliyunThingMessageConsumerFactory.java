package io.github.athingx.athing.platform.builder.message;

import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import jakarta.jms.JMSException;
import jakarta.jms.Session;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

import static io.github.athingx.athing.platform.impl.util.IOUtils.closeQuietly;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * 阿里云设备消息消费者工厂
 */
public class AliyunThingMessageConsumerFactory implements ThingMessageConsumerFactory {

    private String identity;
    private String secret;
    private String remote;
    private String group;
    private JmsConnectionFactory jmsConnectionFactory = new AliyunJmsConnectionFactory();
    private ThingMessageListener listener;

    /**
     * 账号
     *
     * @param identity 账号
     * @return this
     */
    public AliyunThingMessageConsumerFactory identity(String identity) {
        this.identity = identity;
        return this;
    }

    /**
     * 密码
     *
     * @param secret 密码
     * @return this
     */
    public AliyunThingMessageConsumerFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * 消息服务器地址
     *
     * @param remote 消息服务器地址
     * @return this
     */
    public AliyunThingMessageConsumerFactory remote(String remote) {
        this.remote = remote;
        return this;
    }

    /**
     * 消息队列名
     *
     * @param group 消息队列名
     * @return this
     */
    public AliyunThingMessageConsumerFactory group(String group) {
        this.group = group;
        return this;
    }

    /**
     * 消息队列名
     *
     * @param queue 消息队列名
     * @return this
     * @see #group(String)
     */
    public AliyunThingMessageConsumerFactory queue(String queue) {
        this.group = queue;
        return this;
    }

    /**
     * JMS连接工厂
     *
     * @param jmsConnectionFactory JMS连接工厂
     * @return this
     */
    public AliyunThingMessageConsumerFactory connectionFactory(JmsConnectionFactory jmsConnectionFactory) {
        this.jmsConnectionFactory = jmsConnectionFactory;
        return this;
    }

    /**
     * 设备消息监听器
     *
     * @param listener 设备消息监听器
     * @return this
     */
    public AliyunThingMessageConsumerFactory listener(ThingMessageListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public ThingMessageConsumer make() throws JMSException {

        requireNonNull(identity, "identity is required!");
        requireNonNull(secret, "secret is required!");
        requireNonNull(remote, "remote is required!");
        requireNonNull(group, "group is required!");
        requireNonNull(listener, "listener is required!");
        requireNonNull(jmsConnectionFactory, "connection-factory is required!");

        final String uniqueId = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();
        final var connection = jmsConnectionFactory.make(
                remote,
                getUsername(identity, timestamp, uniqueId, group),
                getPassword(identity, secret, timestamp)
        );
        try {
            final var session = connection.createSession(Session.CLIENT_ACKNOWLEDGE);
            final var consumer = session.createConsumer(session.createQueue(group));
            final var name = "thing-message-consumer://%s".formatted(group);
            return new ThingMessageConsumer(name, consumer, listener) {

                @Override
                public void close() throws JMSException {
                    connection.close();
                    super.close();
                }

            };
        } catch (Throwable cause) {
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
