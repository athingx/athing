package io.github.athingx.athing.thing.impl;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.ThingOp;
import io.github.athingx.athing.thing.impl.op.ThingOpImpl;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * 设备实现
 */
public class ThingImpl implements Thing {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingPath path;
    private final IMqttAsyncClient client;
    private final ExecutorService executor;
    private final ThingOp thingOp;

    public ThingImpl(ThingPath path, IMqttAsyncClient client, ExecutorService executor) {
        this.path = path;
        this.client = client;
        this.executor = executor;
        this.thingOp = new ThingOpImpl(path, client, executor);
    }

    @Override
    public ThingPath path() {
        return path;
    }

    @Override
    public ThingOp op() {
        return thingOp;
    }

    @Override
    public ExecutorService executor() {
        return executor;
    }

    @Override
    public void destroy() {

        // 断连MQTT
        try {
            client.disconnect();
            logger.debug("{}/destroy/mqtt/disconnect success!", path);
        } catch (MqttException cause) {
            logger.warn("{}/destroy/mqtt/disconnect failure!", path, cause);
        }

        // 关闭MQTT
        try {
            client.close();
            logger.debug("{}/destroy/mqtt/close success!", path);
        } catch (MqttException cause) {
            logger.warn("{}/destroy/mqtt/close failure!", path, cause);
        }

        // 关闭线程池
        executor.shutdown();
        logger.debug("{}/destroy/executor/shutdown success!", path);

        // 设备关闭完成
        logger.info("{}/destroy success!", path);

    }

}
