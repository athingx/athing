package io.github.athingx.athing.thing.builder;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * MQTT客户端生产工厂
 */
public interface MqttClientFactory {

    /**
     * 生产MQTT客户端
     *
     * @param access 设备访问
     * @return MQTT异步客户端
     * @throws MqttException 生产异常
     */
    IMqttAsyncClient make(ThingAccess access) throws MqttException;

}
