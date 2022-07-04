package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.Thing;
import io.github.athingx.athing.thing.ThingException;
import io.github.athingx.athing.thing.ThingPath;
import io.github.athingx.athing.thing.impl.ThingImpl;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class ThingBuilder {

    private final ThingAccess access;
    private final ThingPath path;
    private MqttClientFactory mcFactory;
    private Function<ThingPath, ExecutorService> executorFactory;

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

        // 检查参数
        Objects.requireNonNull(mcFactory, "client is required!");
        Objects.requireNonNull(executorFactory, "executor is required!");

        final IMqttAsyncClient client = buildingClient();
        final ExecutorService executor = buildingExecutor();

        try {
            return new ThingImpl(path, client, executor);
        } catch (Exception e) {

            // 构建设备失败，需要销毁已分配的资源

            // 销毁MQTT客户端
            try {
                client.close();
            } catch (MqttException mcCause) {
                // ignore
            }

            // 销毁线程池
            executor.shutdown();

            // 继续对外抛出
            throw new ThingException(path, "init thing error!", e);

        }
    }

}
