package io.github.athingx.athing.platform.builder.message;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * 阿里云JMS连接工厂
 */
public class AliyunJmsConnectionFactory implements JmsConnectionFactory {

    private final String uniqueId = UUID.randomUUID().toString();
    private final long timestamp = System.currentTimeMillis();
    private String identity;
    private String secret;
    private String remote;
    private String queue;

    /**
     * 账号
     *
     * @param identity 账号
     * @return this
     */
    public AliyunJmsConnectionFactory identity(String identity) {
        this.identity = identity;
        return this;
    }

    /**
     * 密码
     *
     * @param secret 密码
     * @return this
     */
    public AliyunJmsConnectionFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * 消息服务器地址
     *
     * @param remote 消息服务器地址
     * @return this
     */
    public AliyunJmsConnectionFactory remote(String remote) {
        this.remote = remote;
        return this;
    }

    /**
     * 消息队列名
     *
     * @param queue 消息队列名
     * @return this
     */
    public AliyunJmsConnectionFactory queue(String queue) {
        this.queue = queue;
        return this;
    }

    @Override
    public Connection make() throws JMSException {
        requireNonNull(identity, "identity is required!");
        requireNonNull(secret, "secret is required!");
        requireNonNull(remote, "remote is required!");
        requireNonNull(queue, "queue is required!");
        final Connection connection = new org.apache.qpid.jms.JmsConnectionFactory(
                getUsername(identity, timestamp, uniqueId, queue),
                getPassword(identity, secret, timestamp),
                remote
        ).createConnection();
        connection.start();
        return connection;
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
