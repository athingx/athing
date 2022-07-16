package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.impl.ThingImpl;
import org.eclipse.paho.client.mqttv3.MqttException;

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

    public ThingBuilder(ThingPath path) {
        this.path = path;
    }

    public ThingBuilder client(MqttClientFactory mcFactory) {
        this.mcFactory = mcFactory;
        return this;
    }

    public ThingBuilder executor(ExecutorServiceFactory esFactory) {
        this.esFactory = esFactory;
        return this;
    }

    public Thing build() throws MqttException {
        return new ThingImpl(
                path,
                mcFactory.make(path),
                esFactory.make(path)
        );
    }

}
