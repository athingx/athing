package io.github.athingx.athing.thing.impl.op;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.CompletableFuture;

class MqttClientSupport {

    private final IMqttAsyncClient client;

    protected MqttClientSupport(IMqttAsyncClient client) {
        this.client = client;
    }

    protected CompletableFuture<Void> pahoMqttPublish(String topic, int qos, byte[] payload) {
        final var future = new MqttActionListenerFuture<Void>();
        try {
            client.publish(topic, payload, qos, false, null, future);
        } catch (MqttException cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    protected CompletableFuture<Void> pahoMqttSubscribe(String express, int qos, IMqttMessageListener listener) {
        final var future = new MqttActionListenerFuture<Void>();
        try {
            client.subscribe(express, qos, null, future, listener);
        } catch (MqttException cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

    protected CompletableFuture<Void> pahoMqttUnsubscribe(String express) {
        final var future = new MqttActionListenerFuture<Void>();
        try {
            client.unsubscribe(express, null, future);
        } catch (MqttException cause) {
            future.completeExceptionally(cause);
        }
        return future;
    }

}
