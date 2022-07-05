package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.Thing;
import io.github.athingx.athing.thing.ThingException;
import io.github.athingx.athing.thing.ThingPath;
import io.github.athingx.athing.thing.impl.ThingImpl;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ThingBuilder {

    private final ThingAccess access;
    private final ThingPath path;


    private MqttClientFactory mcFactory;


    private Function<ThingPath, ExecutorService> executorFactory = path-> Executors.newFixedThreadPool(20, new ThreadFactory() {

        private final AtomicInteger indexRef = new AtomicInteger(1000);

        @Override
        public Thread newThread(Runnable r) {
            final Thread worker = new Thread(r, "%s/executor/daemon-%d".formatted(path, indexRef.incrementAndGet()));
            worker.setDaemon(true);
            return worker;
        }

    });

    public ThingBuilder(ThingAccess access) {
        this.access = access;
        this.path = new ThingPath(access.getProductId(), access.getThingId());
    }

    public ThingBuilder client(MqttClientFactory factory) {
        this.mcFactory = Objects.requireNonNull(factory);
        return this;
    }

    public ThingBuilder executor(Function<ThingPath, ExecutorService> factory) {
        this.executorFactory = Objects.requireNonNull(factory);
        return this;
    }

    private IMqttAsyncClient buildingClient() throws ThingException {
        try {
            return Objects.requireNonNull(mcFactory.make(access));
        } catch (MqttException cause) {
            throw new ThingException(path, "init mqtt-client error!", cause);
        }
    }

    private ExecutorService buildingExecutor() {
        return Objects.requireNonNull(executorFactory.apply(path));
    }

    public Thing build() throws ThingException {
        Objects.requireNonNull(mcFactory, "client is required!");
        Objects.requireNonNull(executorFactory, "executor is required!");
        return new ThingImpl(
                path,
                Objects.requireNonNull(buildingClient()),
                Objects.requireNonNull(buildingExecutor())
        );
    }

}
