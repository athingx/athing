package io.github.athingx.athing.thing.impl;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.ThingOp;
import io.github.athingx.athing.thing.api.plugin.ThingPlugin;
import io.github.athingx.athing.thing.api.plugin.ThingPluginInstaller;
import io.github.athingx.athing.thing.impl.op.ThingOpImpl;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private final ThingOp thingOp;
    private final CompletableFuture<Void> destroyF = new CompletableFuture<>();
    private final Map<String, PluginStub> pluginStubMap = new ConcurrentHashMap<>();

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
    public Executor executor() {
        return executor;
    }

    private record PluginStub(ThingPluginInstaller.Meta<?> meta, CompletableFuture<?> future) {
    }

    @Override
    public <T extends ThingPlugin> CompletableFuture<T> install(ThingPluginInstaller<T> installer) {

        synchronized (pluginStubMap) {

            // 检查是否已经安装
            if (pluginStubMap.containsKey(installer.meta().name())) {
                throw new IllegalStateException("%s already installed!".formatted(installer.meta()));
            }

            // 安装结果
            final var installF = installer.install(this)
                    .whenComplete((plugin, ex) -> {
                        logger.debug("{}/plugin/{} install completed!", path, installer.meta().name(), ex);
                        if (Objects.nonNull(plugin)) {
                            destroyF.thenAccept(unused -> plugin.uninstall().whenComplete((v, unEx) ->
                                    logger.debug("{}/plugin/{} uninstall completed!", path, installer.meta().name(), unEx)));
                        }
                    });

            // 注册安装结果
            pluginStubMap.put(installer.meta().name(), new PluginStub(installer.meta(), installF));

            // 返回安装结果
            return installF;

        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ThingPlugin> Optional<CompletableFuture<T>> plugin(String name, Class<T> type) {
        return Optional.ofNullable(pluginStubMap.get(name))
                .filter(stub -> Objects.equals(type, stub.meta.type()))
                .map(stub -> (CompletableFuture<T>) stub.future());
    }

    @Override
    public boolean isDestroyed() {
        return destroyF.isDone();
    }

    @Override
    public void destroy() {

        // 尝试进行首次关闭
        if (destroyF.complete(null)) {

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

        // 设备无法被重复关闭
        else {
            throw new IllegalStateException("%s already destroyed!".formatted(path));
        }

    }

}
