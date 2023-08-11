package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.client.MqttClientFactory;
import io.github.athingx.athing.thing.builder.executor.DefaultExecutorServiceFactory;
import io.github.athingx.athing.thing.builder.executor.ExecutorServiceFactory;
import io.github.athingx.athing.thing.impl.ThingImpl;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

import java.util.concurrent.ExecutorService;

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
     * 设置MQTT客户端
     *
     * @param client MQTT客户端
     * @return this
     */
    public ThingBuilder client(IMqttAsyncClient client) {
        this.mcFactory = path -> client;
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
     * 设置线程池
     *
     * @param executor 线程池
     * @return this
     */
    public ThingBuilder executor(ExecutorService executor) {
        this.esFactory = path -> executor;
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
                requireNonNull(mcFactory.make(path), "mqtt-client is required!"),
                requireNonNull(esFactory.make(path), "executor is required!")
        );
    }

}
