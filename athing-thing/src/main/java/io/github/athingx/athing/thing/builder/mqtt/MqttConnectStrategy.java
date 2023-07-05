package io.github.athingx.athing.thing.builder.mqtt;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 连接策略
 */
@FunctionalInterface
public interface MqttConnectStrategy {

    /**
     * 连接
     *
     * @param isReconnect 是否重连
     * @param path        设备路径
     * @param options     Mqtt Options
     * @param client      Mqtt Client
     * @throws MqttException 连接失败
     */
    void connect(boolean isReconnect, ThingPath path, MqttConnectOptions options, IMqttAsyncClient client) throws MqttException;

    /**
     * 有限重试
     *
     * @param reTryLimits 重试次数
     * @return 有限重试连接策略
     */
    static MqttConnectStrategy limitsReTry(int reTryLimits) {
        return new MqttConnectStrategy() {

            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void connect(boolean isReconnect, ThingPath path, MqttConnectOptions options, IMqttAsyncClient client) throws MqttException {
                final var reTryIntervalMs = options.getMaxReconnectDelay() * 1000L;
                final var lock = new ReentrantLock();
                final var waiting = lock.newCondition();
                final var action = isReconnect ? "reconnect" : "connect";
                int reTryTimes = 0;
                while (true) {
                    try {
                        client.connect(options).waitForCompletion();
                        logger.debug("{}/mqtt/client {} success!", path, action);
                        break;
                    } catch (Exception cause) {

                        // 如果重试次数超过限定，则将本次失败抛出
                        if (reTryLimits > 0 && reTryTimes++ >= reTryLimits) {
                            if (cause instanceof MqttException mqCause) {
                                throw mqCause;
                            }
                            throw new MqttException(cause);
                        }

                        logger.warn("{}/mqtt/client {} failure, will retry after {}ms, limits: {}/{}",
                                path,
                                action,
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

            }
        };
    }

    /**
     * 永久重试
     *
     * @return 永久重试连接策略
     */
    static MqttConnectStrategy alwaysReTry() {
        return new MqttConnectStrategy() {

            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void connect(boolean isReconnect, ThingPath path, MqttConnectOptions options, IMqttAsyncClient client) {
                final var reTryIntervalMs = options.getMaxReconnectDelay() * 1000L;
                final var lock = new ReentrantLock();
                final var waiting = lock.newCondition();
                final var action = isReconnect ? "reconnect" : "connect";
                while (true) {
                    try {
                        client.connect(options).waitForCompletion();
                        logger.debug("{}/mqtt/client {} success!", path, action);
                        break;
                    } catch (Exception cause) {
                        logger.warn("{}/mqtt/client {} failure, will retry after {}ms", path, action, reTryIntervalMs, cause);
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
            }
        };
    }

}
