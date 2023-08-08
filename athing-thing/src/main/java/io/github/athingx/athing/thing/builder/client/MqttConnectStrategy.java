package io.github.athingx.athing.thing.builder.client;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MQTT连接策略
 */
public interface MqttConnectStrategy {

    /**
     * 连接
     *
     * @param path        设备路径
     * @param client      Mqtt Client
     * @param options     Mqtt Options
     * @param isReconnect 是否重连
     * @throws MqttException 连接失败
     */
    void connect(ThingPath path, IMqttAsyncClient client, MqttConnectOptions options, boolean isReconnect) throws MqttException;

    /**
     * 重试连接策略
     *
     * @param limit          重试次数限制
     * @param stepIntervalMs 重试间隔(ms)
     * @param maxIntervalMs  重试最大间隔(ms)
     * @return 重试连接策略
     */
    static MqttConnectStrategy retry(int limit, long stepIntervalMs, long maxIntervalMs) {
        return new MqttConnectStrategy() {

            private final Logger logger = LoggerFactory.getLogger(getClass());
            private final ReentrantLock lock = new ReentrantLock();
            private final Condition waiting = lock.newCondition();
            private final AtomicInteger retriesRef = new AtomicInteger();

            /**
             * 是否到达重试次数限制
             * @param retries 重试次数
             * @param limit 重试次数限制
             * @return TRUE | FALSE
             */
            private static boolean isLimit(int retries, int limit) {
                return limit > 0 && retries >= limit;
            }

            /**
             * 计算重试间隔
             * @param retries 重试次数
             * @param stepIntervalMs 每次重试间隔(ms)
             * @param maxIntervalMs 最大重试间隔(ms)
             * @return 重试间隔(ms)
             */
            private static long computeIntervalMs(int retries, long stepIntervalMs, long maxIntervalMs) {
                return Math.min(maxIntervalMs, stepIntervalMs * (retries + 1));
            }

            @Override
            public void connect(ThingPath path, IMqttAsyncClient client, MqttConnectOptions options, boolean isReconnect) throws MqttException {

                // 尝试进行连接
                try {
                    client.connect(options).waitForCompletion();
                }

                // 连接失败进入重试
                catch (MqttException cause) {

                    // 动作标签
                    final var action = isReconnect ? "reconnect" : "connect";

                    // 重试计数
                    final var retries = retriesRef.getAndIncrement();

                    // 如果到达重试次数限制，则不再重试
                    if (isLimit(retries, limit)) {
                        logger.warn("{}/mqtt {} reach limit, give up retry! retries={};limit={};", path, action, retries, limit);
                        throw cause;
                    }

                    // 计算下次重试间隔
                    final var intervalMs = computeIntervalMs(retries, stepIntervalMs, maxIntervalMs);
                    logger.warn("{}/mqtt {} failure, will retry after {}ms. retries={};", path, action, intervalMs, retries, cause);

                    // 进入等待休眠
                    lock.lock();
                    try {
                        if (waiting.await(intervalMs, TimeUnit.MILLISECONDS)) {
                            // 等待被提前唤醒，这不可能发生
                            logger.warn("{}/mqtt {} is waiting, but was wakeup! retries={}", path, action, retries);
                        }
                    }

                    // 等待被中断，有可能是被外部关闭
                    catch (InterruptedException iCause) {
                        logger.warn("{}/mqtt {} is waiting, but was interrupted! retries={}", path, action, retries);
                        Thread.currentThread().interrupt();
                        throw cause;
                    } finally {
                        lock.unlock();
                    }

                    // 这里真正进行重试
                    connect(path, client, options, isReconnect);

                } // try-catch end

            } // connect end

        }; // retry end

    }


    /**
     * 永久重试
     *
     * @param stepIntervalMs 每次重试间隔(ms)
     * @param maxIntervalMs  最大重试间隔(ms)
     * @return 永久重试连接策略
     */
    static MqttConnectStrategy always(long stepIntervalMs, long maxIntervalMs) {
        return retry(-1, stepIntervalMs, maxIntervalMs);
    }

}
