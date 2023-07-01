package io.github.athingx.athing.thing.builder.mqtt;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 连接策略
 */
@FunctionalInterface
public interface MqttConnectStrategy {

    /**
     * 连接
     *
     * @param path    设备路径
     * @param options Mqtt Options
     * @param client  Mqtt Client
     * @throws MqttException 连接失败
     */
    void connect(ThingPath path, MqttConnectOptions options, IMqttAsyncClient client) throws MqttException;

    /**
     * 异步连接策略
     *
     * @param executor 线程池
     * @param strategy 连接策略
     * @return 异步连接策略
     */
    static MqttConnectStrategy async(ExecutorService executor, MqttConnectStrategy strategy) {
        return (path, options, client) -> executor.execute(() -> {
            try {
                strategy.connect(path, options, client);
            } catch (MqttException cause) {
                throw new RuntimeException(cause);
            }
        });
    }

    /**
     * 异步连接策略
     *
     * @param factory  线程工厂
     * @param strategy 连接策略
     * @return 异步连接策略
     */
    static MqttConnectStrategy async(ThreadFactory factory, MqttConnectStrategy strategy) {
        return (path, options, client) -> factory.newThread(() -> {
            try {
                strategy.connect(path, options, client);
            } catch (MqttException cause) {
                throw new RuntimeException(cause);
            }
        }).start();
    }

    /**
     * 异步连接策略
     *
     * @param strategy 连接策略
     * @return 异步连接策略
     */
    static MqttConnectStrategy async(MqttConnectStrategy strategy) {
        return async(r -> new Thread(r) {{
            setName("athing-thing-mqtt-connect");
            setDaemon(true);
        }}, strategy);
    }

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
            public void connect(ThingPath path, MqttConnectOptions options, IMqttAsyncClient client) throws MqttException {
                final long reTryIntervalMs = options.getMaxReconnectDelay() * 1000L;
                final ReentrantLock lock = new ReentrantLock();
                final Condition waiting = lock.newCondition();
                int reTryTimes = 0;
                while (true) {
                    try {
                        client.connect(options).waitForCompletion();
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
            public void connect(ThingPath path, MqttConnectOptions options, IMqttAsyncClient client) {
                final long reTryIntervalMs = options.getMaxReconnectDelay() * 1000L;
                final ReentrantLock lock = new ReentrantLock();
                final Condition waiting = lock.newCondition();
                while (true) {
                    try {
                        client.connect(options).waitForCompletion();
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
            }
        };
    }

}
