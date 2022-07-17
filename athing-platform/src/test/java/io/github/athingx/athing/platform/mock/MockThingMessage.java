package io.github.athingx.athing.platform.mock;

import io.github.athingx.athing.platform.api.message.ThingMessage;

public class MockThingMessage extends ThingMessage {

    /**
     * 设备消息
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     */
    public MockThingMessage(String productId, String thingId, long timestamp) {
        super(productId, thingId, timestamp);
    }

}
