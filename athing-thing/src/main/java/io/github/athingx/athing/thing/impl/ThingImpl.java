package io.github.athingx.athing.thing.impl;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.Codec;
import io.github.athingx.athing.thing.api.op.ThingOp;
import io.github.athingx.athing.thing.api.plugin.ThingPlugins;
import io.github.athingx.athing.thing.impl.op.ThingOpImpl;
import io.github.athingx.athing.thing.plugin.ThingPluginsImpl;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 设备实现
 */
public class ThingImpl implements Thing {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingPath path;
    private final IMqttAsyncClient client;
    private final ExecutorService executor;
    private final ThingOp<byte[], byte[]> thingOp;
    private final ThingPlugins plugins;
    private final CompletableFuture<Void> destroyF = new CompletableFuture<>();

    public ThingImpl(ThingPath path, IMqttAsyncClient client, ExecutorService executor) {
        this.path = path;
        this.client = client;
        this.executor = executor;
        this.thingOp = new ThingOpImpl<>(path, client, executor, Codec.none());
        this.plugins = new ThingPluginsImpl(this, destroyF);
    }

    @Override
    public ThingPath path() {
        return path;
    }

    @Override
    public ThingOp<byte[], byte[]> op() {
        return thingOp;
    }

    @Override
    public Executor executor() {
        return executor;
    }

    @Override
    public ThingPlugins plugins() {
        return plugins;
    }


    @Override
    public boolean isDestroyed() {
        return destroyF.isDone();
    }

    @Override
    public void destroy() {

        if (!destroyF.complete(null)) {
            throw new IllegalStateException("already destroyed!");
        }

        // 断连MQTT
        try {
            client.disconnect();
        } catch (MqttException cause) {
            logger.warn("{}/destroy/mqtt/disconnect failure!", path, cause);
        }

        // 关闭MQTT
        try {
            client.close();
        } catch (MqttException cause) {
            logger.warn("{}/destroy/mqtt/close failure!", path, cause);
        }

        // 关闭线程池
        executor.shutdown();

    }

}
