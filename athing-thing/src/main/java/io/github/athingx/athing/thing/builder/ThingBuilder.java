package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.client.MqttClientFactory;
import io.github.athingx.athing.thing.builder.executor.DefaultExecutorServiceFactory;
import io.github.athingx.athing.thing.builder.executor.ExecutorServiceFactory;
import io.github.athingx.athing.thing.impl.ThingImpl;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * 设备构造器
 */
public class ThingBuilder {

    private final ThingPath path;

    private ExecutorServiceFactory esFactory = new DefaultExecutorServiceFactory();

    private MqttClientFactory mcFactory = path -> null;

    /**
     * 设备构造器
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     */
    public ThingBuilder(String productId, String thingId) {
        this(new ThingPath(productId, thingId));
    }

    /**
     * 设备构造器
     *
     * @param path 设备路径
     */
    public ThingBuilder(ThingPath path) {
        this.path = path;
    }

    /**
     * 设置MQTT客户端
     *
     * @param mcFactory MQTT客户端工厂
     * @return this
     */
    public ThingBuilder client(MqttClientFactory mcFactory) {
        this.mcFactory = mcFactory;
        return this;
    }

    /**
     * 设置线程池
     *
     * @param esFactory 线程池工厂
     * @return this
     */
    public ThingBuilder executor(ExecutorServiceFactory esFactory) {
        this.esFactory = esFactory;
        return this;
    }

    /**
     * 构造设备
     *
     * @return 设备
     * @throws Exception 构造失败
     */
    public Thing build() throws Exception {
        return new ThingImpl(
                path,
                requireNonNull(mcFactory.make(path), "client is required!"),
                requireNonNull(esFactory.make(path), "executor is required!")
        );
    }

    /**
     * 异步构造设备
     *
     * @return 设备构造操作
     */
    public CompletableFuture<Thing> asyncBuild() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
