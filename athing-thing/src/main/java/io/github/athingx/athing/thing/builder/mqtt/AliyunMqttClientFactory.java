package io.github.athingx.athing.thing.builder.mqtt;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static io.github.athingx.athing.thing.builder.mqtt.MqttConnectStrategy.alwaysReTry;
import static io.github.athingx.athing.thing.impl.util.StringUtils.bytesToHexString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * 阿里云MQTT客户端工厂
 */
public class AliyunMqttClientFactory implements MqttClientFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String remote;
    private String secret;
    private MqttConnectOptions options = new MqttConnectOptions() {{
        setCleanSession(false);         // 关闭清理会话
        setAutomaticReconnect(false);   // 关闭自动重连
        setKeepAliveInterval(60 * 30);  // 30分钟心跳
        setConnectionTimeout(30);       // 30秒连接超时
        setMaxReconnectDelay(60);       // 60秒最大重连间隔
    }};

    private MqttConnectStrategy strategy;

    public AliyunMqttClientFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    public AliyunMqttClientFactory remote(String remote) {
        this.remote = remote;
        return this;
    }

    public AliyunMqttClientFactory options(Function<MqttConnectOptions, MqttConnectOptions> optionsFn) {
        this.options = optionsFn.apply(options);
        return this;
    }

    public AliyunMqttClientFactory strategy(MqttConnectStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    @Override
    public IMqttAsyncClient make(ThingPath path) throws MqttException {
        requireNonNull(remote, "remote is required!");
        requireNonNull(secret, "secret is required!");

        // 构建MQTT连接策略
        final var strategy = Optional.ofNullable(this.strategy).orElse(alwaysReTry());

        // 构建阿里云鉴权信息
        final var access = new Access(path);

        // 构建MQTT客户端
        final var client = new MqttAsyncClient(remote, access.getClientId(), new MemoryPersistence());

        // 构建MQTT选项
        final var options = Optional.ofNullable(this.options).orElse(new MqttConnectOptions());
        options.setUserName(access.getUsername());
        options.setPassword(access.getPassword(secret));

        client.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean b, String s) {
                logger.info("{}/mqtt/client connect completed!", path);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                try {
                    logger.warn("{}/mqtt/client connect lost, reconnecting...", path, throwable);
                    strategy.connect(path, options, client);
                } catch (MqttException cause) {
                    logger.error("{}/mqtt/client connect lost!", path, cause);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });

        // 根据连接策略进行连接
        strategy.connect(path, options, client);

        return client;
    }

    /**
     * 阿里云鉴权信息
     */
    private static class Access {

        final String uniqueId = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();
        final ThingPath path;

        /**
         * 构建启动信息
         *
         * @param path 设备路径
         */
        Access(ThingPath path) {
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

}
