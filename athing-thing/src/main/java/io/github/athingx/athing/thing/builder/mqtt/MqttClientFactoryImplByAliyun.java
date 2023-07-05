package io.github.athingx.athing.thing.builder.mqtt;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;
import java.util.function.Function;

import static io.github.athingx.athing.thing.builder.mqtt.MqttConnectStrategy.alwaysReTry;
import static io.github.athingx.athing.thing.impl.util.StringUtils.bytesToHexString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * 阿里云MQTT客户端工厂
 */
public class MqttClientFactoryImplByAliyun implements MqttClientFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String remote;
    private String secret;

    private MqttConnectStrategy strategy = alwaysReTry();
    private MqttClientPersistence persistence = new MemoryPersistence();

    private MqttConnectOptions connOpt = new MqttConnectOptions() {{
        setCleanSession(false);         // 关闭清理会话
        setAutomaticReconnect(false);   // 关闭自动重连
        setKeepAliveInterval(60 * 30);  // 30分钟心跳
        setConnectionTimeout(30);       // 30秒连接超时
        setMaxReconnectDelay(60);       // 60秒最大重连间隔
    }};

    private DisconnectedBufferOptions bufferOpt = new DisconnectedBufferOptions() {{
        setBufferEnabled(true);         // 开启缓冲区
        setPersistBuffer(false);        // 持久化缓冲区
        setBufferSize(5000);            // 缓冲区大小
        setDeleteOldestMessages(true);  // 删除最旧的消息
    }};

    public MqttClientFactoryImplByAliyun secret(String secret) {
        this.secret = requireNonNull(secret, "secret is required!");
        return this;
    }

    public MqttClientFactoryImplByAliyun remote(String remote) {
        this.remote = requireNonNull(remote, "remote is required!");
        return this;
    }

    public MqttClientFactoryImplByAliyun connOpt(Function<MqttConnectOptions, MqttConnectOptions> connOptFn) {
        return connOpt(connOptFn.apply(connOpt));
    }

    public MqttClientFactoryImplByAliyun connOpt(MqttConnectOptions connOpt) {
        this.connOpt = requireNonNull(connOpt, "connOpt is required!");
        return this;
    }

    public MqttClientFactoryImplByAliyun bufferOpt(Function<DisconnectedBufferOptions, DisconnectedBufferOptions> bufferOptFn) {
        return bufferOpt(bufferOptFn.apply(bufferOpt));
    }

    public MqttClientFactoryImplByAliyun bufferOpt(DisconnectedBufferOptions bufferOpt) {
        this.bufferOpt = requireNonNull(bufferOpt, "bufferOpt is required!");
        return this;
    }

    public MqttClientFactoryImplByAliyun strategy(MqttConnectStrategy strategy) {
        this.strategy = requireNonNull(strategy, "strategy is required!");
        return this;
    }

    public MqttClientFactoryImplByAliyun persistence(MqttClientPersistence persistence) {
        this.persistence = requireNonNull(persistence, "persistence is required!");
        return this;
    }

    @Override
    public IMqttAsyncClient make(ThingPath path) throws MqttException {
        requireNonNull(remote, "remote is required!");
        requireNonNull(secret, "secret is required!");

        // 构建阿里云MQTT签名
        final var sign = new MqttSign(path);
        connOpt.setUserName(requireNonNullElse(connOpt.getUserName(), sign.getUsername()));
        connOpt.setPassword(requireNonNullElse(connOpt.getPassword(), sign.getPassword(secret)));

        // 构建MQTT客户端
        final var client = new MqttAsyncClient(remote, sign.getClientId(), persistence);
        client.setBufferOpts(bufferOpt);
        client.setCallback(new MqttCallbackAdapter() {
            @Override
            public void connectComplete(boolean isReconnect, String serverURI) {
                logger.info("{}/mqtt/client connect completed! remote={};", path, serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.info("{}/mqtt/client connection lost! will be reconnect!", path, cause);

                // use mqtt self reconnect
                if (connOpt.isAutomaticReconnect()) {
                    return;
                }

                // use strategy reconnect
                try {
                    strategy.connect(true, path, connOpt, client);
                } catch (MqttException ex) {
                    logger.warn("{}/mqtt/client reconnect failed!", path, ex);
                }

            }

        });

        // 根据连接策略进行连接
        strategy.connect(false, path, connOpt, client);

        return client;
    }

    /**
     * MQTT回调适配器
     */
    private static class MqttCallbackAdapter implements MqttCallbackExtended {

        @Override
        public void connectComplete(boolean isReconnect, String serverURI) {

        }

        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

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
