package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.executor.ExecutorServiceFactory;
import io.github.athingx.athing.thing.builder.mqtt.MqttClientFactory;
import io.github.athingx.athing.thing.impl.ThingImpl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * 设备构造器
 */
public class ThingBuilder {

    private final ThingPath path;
    private MqttClientFactory mcFactory;
    private ExecutorServiceFactory esFactory;

    /**
     * 设备构造器
     *
     * @param path 设备路径
     */
    public ThingBuilder(ThingPath path) {
        this.path = path;
    }

    public ThingBuilder(String productId, String thingId) {
        this(new ThingPath(productId, thingId));
    }

    /**
     * MQTT客户端工厂
     *
     * @param mcFactory MQTT客户端工厂
     * @return this
     */
    public ThingBuilder clientFactory(MqttClientFactory mcFactory) {
        this.mcFactory = mcFactory;
        return this;
    }

    /**
     * 线程池工厂
     *
     * @param esFactory 线程池工厂
     * @return this
     */
    public ThingBuilder executorFactory(ExecutorServiceFactory esFactory) {
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

        // 检查客户端工厂
        Objects.requireNonNull(mcFactory, "mqtt client factory can not be null!");

        // 构建线程池工厂
        esFactory = Objects.nonNull(esFactory) ? esFactory : path -> {
            final AtomicInteger seqRef = new AtomicInteger(1000);
            return newFixedThreadPool(20, r ->
                    new Thread(r) {{
                        setDaemon(true);
                        setName("%s/executor-%d".formatted(path, seqRef.incrementAndGet()));
                    }});
        };

        // 创建设备实例
        return new ThingImpl(
                path,
                mcFactory.make(path),
                esFactory.make(path)
        );
    }

}
