package io.github.athingx.athing.thing.builder.client;

import io.github.athingx.athing.thing.api.ThingPath;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * MQTT客户端工厂
 */
public interface MqttClientFactory {

    /**
     * 创建MQTT客户端
     *
     * @param path 设备路径
     * @return MQTT客户端
     * @throws MqttException 创建失败
     */
    IMqttAsyncClient make(ThingPath path) throws MqttException;

}
