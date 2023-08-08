package io.github.athingx.athing.thing.impl.op;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.concurrent.CompletableFuture;

/**
 * MQTT回调-Future封装
 * @param <T> 成功返回数据类型
 */
class MqttActionListenerFuture<T> extends CompletableFuture<T> implements IMqttActionListener {

    private final T target;

    public MqttActionListenerFuture() {
        this(null);
    }

    public MqttActionListenerFuture(T target) {
        this.target = target;
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        complete(target);
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        completeExceptionally(exception);
    }

}
