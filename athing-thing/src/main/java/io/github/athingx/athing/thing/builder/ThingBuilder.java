package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.executor.ExecutorServiceFactory;
import io.github.athingx.athing.thing.builder.mqtt.MqttClientFactory;
import io.github.athingx.athing.thing.impl.ThingImpl;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * 设备构造器
 */
public class ThingBuilder {

    private final AtomicInteger seqRef = new AtomicInteger(1000);
    private final ThingPath path;
    private MqttClientFactory mcFactory;
    private ExecutorServiceFactory esFactory = path -> newFixedThreadPool(20, r ->
            new Thread(r) {{
                setDaemon(true);
                setName("%s/executor-%d".formatted(path, seqRef.incrementAndGet()));
            }});

    /**
     * 设备构造器
     *
     * @param path 设备路径
     */
    public ThingBuilder(ThingPath path) {
        this.path = path;
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
     * MQTT客户端
     *
     * @param client MQTT客户端
     * @return this
     */
    public ThingBuilder client(IMqttAsyncClient client) {
        return clientFactory(path -> client);
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
     * 线程池
     *
     * @param executor 线程池
     * @return this
     */
    public ThingBuilder executor(ExecutorService executor) {
        return executorFactory(path -> executor);
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
                mcFactory.make(path),
                esFactory.make(path)
        );
    }

}
