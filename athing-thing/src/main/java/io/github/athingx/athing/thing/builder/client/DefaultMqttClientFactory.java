package io.github.athingx.athing.thing.builder.client;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static io.github.athingx.athing.common.util.StringUtils.bytesToHexString;
import static io.github.athingx.athing.thing.builder.client.MqttConnectStrategy.always;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultMqttClientFactory implements MqttClientFactory {

    private String remote;
    private String secret;

    private MqttConnectStrategy strategy = always(30 * 1000L, 3 * 60 * 1000L);

    private MqttClientPersistence persistable = new MemoryPersistence();

    private MqttConnectOptions connOpt = new MqttConnectOptions() {{
        setCleanSession(false);         // 关闭清理会话
        setAutomaticReconnect(false);   // 关闭自动重连
        setKeepAliveInterval(60);       // 60秒心跳
        setConnectionTimeout(30);       // 30秒连接超时
        setMaxReconnectDelay(600);      // 10分钟最大重连间隔
        setExecutorServiceTimeout(1);   // 1秒线程池超时
    }};

    private DisconnectedBufferOptions bufferOpt = new DisconnectedBufferOptions() {{
        setBufferEnabled(true);         // 开启缓冲区
        setPersistBuffer(false);        // 持久化缓冲区
        setBufferSize(5000);            // 缓冲区大小
        setDeleteOldestMessages(true);  // 删除最旧的消息
    }};

    public DefaultMqttClientFactory strategy(MqttConnectStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public DefaultMqttClientFactory persistable(MqttClientPersistence persistable) {
        this.persistable = persistable;
        return this;
    }

    public DefaultMqttClientFactory connOpt(MqttConnectOptions connOpt) {
        this.connOpt = connOpt;
        return this;
    }

    public DefaultMqttClientFactory connOpt(Function<MqttConnectOptions, MqttConnectOptions> factory) {
        Objects.requireNonNull(factory);
        return connOpt(factory.apply(connOpt));
    }

    public DefaultMqttClientFactory bufferOpt(DisconnectedBufferOptions bufferOpt) {
        this.bufferOpt = bufferOpt;
        return this;
    }

    public DefaultMqttClientFactory bufferOpt(Function<DisconnectedBufferOptions, DisconnectedBufferOptions> factory) {
        Objects.requireNonNull(factory);
        return bufferOpt(factory.apply(bufferOpt));
    }

    public DefaultMqttClientFactory remote(String remote) {
        this.remote = remote;
        return this;
    }

    public DefaultMqttClientFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public IMqttAsyncClient make(ThingPath path) throws MqttException {

        Objects.requireNonNull(remote, "remote is required");
        Objects.requireNonNull(secret, "secret is required");

        final var sign = new MqttSign(path);
        final var client = new MqttAsyncClient(remote, sign.getClientId(), persistable);

        // 设置离线缓存选项
        if (Objects.nonNull(bufferOpt)) {
            client.setBufferOpts(bufferOpt);
        }

        // 设置连接选项
        connOpt.setUserName(Objects.requireNonNullElse(connOpt.getUserName(), sign.getUsername()));
        connOpt.setPassword(Objects.requireNonNullElse(connOpt.getPassword(), sign.getPassword(secret)));

        // 设置连接回调
        client.setCallback(new MqttCallbackExtended() {

            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void connectComplete(boolean isReconnect, String serverURI) {
                logger.info("{}/mqtt {} success, remote={}", path, isReconnect ? "reconnect" : "connect", serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {

                // 连接丢失时，如果用户设置了自动重连，则不做任何处理
                if (connOpt.isAutomaticReconnect()) {
                    logger.warn("{}/mqtt connection lost, will retry by automatic reconnect.", path);
                    return;
                }

                // 连接丢失时，如果用户没有设置自动重连，则启用策略进行重连
                try {
                    logger.warn("{}/mqtt connection lost, will retry by strategy.", path);
                    strategy.connect(path, client, connOpt, true);
                } catch (Exception sCause) {
                    logger.error("{}/mqtt connection lost, retry by strategy failed!", path, sCause);
                }

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                // ignore
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // ignore
            }

        });

        // 连接并返回MQTT客户端
        strategy.connect(path, client, connOpt, false);
        return client;
    }

    /**
     * 阿里云MQTT签名
     */
    private static class MqttSign {

        final String uniqueId = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();
        final ThingPath path;

        /**
         * 构建启动信息
         *
         * @param path 设备路径
         */
        MqttSign(ThingPath path) {
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
