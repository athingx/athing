package io.github.athingx.athing.thing.builder.mqtt;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static io.github.athingx.athing.thing.impl.util.StringUtils.bytesToHexString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class AliyunMqttClientFactory implements MqttClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(AliyunMqttClientFactory.class);
    private String remote;
    private String secret;
    private long connectTimeoutMs = 30L * 1000;
    private long keepAliveIntervalMs = 60L * 1000;
    private long maxReconnectDelayMs = 30L * 1000;
    private ConnectStrategy strategy;

    public AliyunMqttClientFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    public AliyunMqttClientFactory remote(String remote) {
        this.remote = remote;
        return this;
    }

    public AliyunMqttClientFactory connectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        return this;
    }

    public AliyunMqttClientFactory keepAliveIntervalMs(long keepAliveIntervalMs) {
        this.keepAliveIntervalMs = keepAliveIntervalMs;
        return this;
    }

    public AliyunMqttClientFactory maxReconnectDelayMs(long maxReconnectDelayMs) {
        this.maxReconnectDelayMs = maxReconnectDelayMs;
        return this;
    }

    public AliyunMqttClientFactory strategy(ConnectStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    @Override
    public IMqttAsyncClient make(ThingPath path) throws MqttException {
        requireNonNull(remote, "remote is required!");
        requireNonNull(secret, "secret is required!");
        strategy = Objects.isNull(strategy) ? ConnectStrategy.alwaysReTry(maxReconnectDelayMs) : strategy;
        final Boot boot = new Boot(path);
        final MqttConnectOptions options = new MqttConnectOptions() {{
            setUserName(boot.getUsername());
            setPassword(boot.getPassword(secret));
            setCleanSession(true);
            setAutomaticReconnect(false);
            setConnectionTimeout((int) (connectTimeoutMs / 1000));
            setKeepAliveInterval((int) (keepAliveIntervalMs / 1000));
            setMaxReconnectDelay((int) (maxReconnectDelayMs / 1000));
        }};
        final IMqttAsyncClient client = new MqttAsyncClient(remote, boot.getClientId(), new MemoryPersistence());

        // 根据连接策略进行连接
        strategy.connect(path, options, client);
        return client;
    }

    /**
     * 启动信息
     */
    private static class Boot {

        final String uniqueId = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();
        final ThingPath path;

        /**
         * 构建启动信息
         *
         * @param path 设备路径
         */
        Boot(ThingPath path) {
            this.path = path;
        }

        /**
         * 获取MQTT帐号
         *
         * @return MQTT帐号
         */
        String getUsername() {
            return "%s&%s".formatted(path.getThingId(), path.getProductId());
        }

        /**
         * 获取MQTT密码
         *
         * @return MQTT密码
         */
        char[] getPassword(String secret) {
            final String content = "clientId%sdeviceName%sproductKey%stimestamp%s".formatted(
                    uniqueId,
                    path.getThingId(),
                    path.getProductId(),
                    timestamp
            );
            try {
                final Mac mac = Mac.getInstance("HMACSHA1");
                mac.init(new SecretKeySpec(secret.getBytes(UTF_8), mac.getAlgorithm()));
                return bytesToHexString(mac.doFinal(content.getBytes(UTF_8))).toCharArray();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 获取MQTT客户端ID
         *
         * @return 客户端ID
         */
        String getClientId() {
            return "%s|securemode=3,signmethod=hmacsha1,timestamp=%s,ext=1|".formatted(uniqueId, timestamp);
        }

    }

    /**
     * 连接策略
     */
    @FunctionalInterface
    public interface ConnectStrategy {

        void connect(ThingPath path, MqttConnectOptions options, IMqttAsyncClient client) throws MqttException;

        /**
         * 有限重试
         *
         * @param reTryLimits 重试次数
         * @return 连接策略
         */
        static ConnectStrategy limitsReTry(int reTryLimits, long reTryIntervalMs) {
            return (path, options, client) -> {
                final ReentrantLock lock = new ReentrantLock();
                final Condition waiting = lock.newCondition();
                int reTryTimes = 0;
                while (true) {
                    try {
                        final IMqttToken token = client.connect(options);
                        token.waitForCompletion();
                        if (Objects.nonNull(token.getException())) {
                            throw token.getException();
                        }
                        logger.debug("{}/mqtt/client connect success!", path);
                        break;
                    } catch (Exception cause) {

                        // 如果重试次数超过限定，则将本次失败抛出
                        if (reTryLimits > 0 && reTryTimes++ >= reTryLimits) {
                            if (cause instanceof MqttException mqCause) {
                                throw mqCause;
                            }
                            throw new MqttException(cause);
                        }

                        logger.warn("{}/mqtt/client connect failure, will reconnect after {}ms, limits: {}/{}",
                                path,
                                reTryIntervalMs,
                                reTryTimes,
                                reTryLimits,
                                cause
                        );
                        lock.lock();
                        try {
                            if (waiting.await(reTryIntervalMs, TimeUnit.MILLISECONDS)) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            break;
                        } finally {
                            lock.unlock();
                        }
                    }
                }// while

            };
        }

        /**
         * 永久重试
         *
         * @param reTryIntervalMs 重试间隔
         * @return 连接策略
         */
        static ConnectStrategy alwaysReTry(long reTryIntervalMs) {
            return (path, options, client) -> {
                final ReentrantLock lock = new ReentrantLock();
                final Condition waiting = lock.newCondition();
                while (true) {
                    try {
                        final IMqttToken token = client.connect(options);
                        token.waitForCompletion();
                        if (Objects.nonNull(token.getException())) {
                            throw token.getException();
                        }
                        logger.debug("{}/mqtt/client connect success!", path);
                        break;
                    } catch (Exception cause) {
                        logger.warn("{}/mqtt/client connect failure, will reconnect after {}ms",
                                path,
                                reTryIntervalMs,
                                cause
                        );
                        lock.lock();
                        try {
                            if (waiting.await(reTryIntervalMs, TimeUnit.MILLISECONDS)) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            break;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            };
        }

    }

}
