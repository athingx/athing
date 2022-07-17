package io.github.athingx.athing.platform.api.message;

/**
 * 设备消息
 */
public class ThingMessage {

    private final String productId;
    private final String thingId;
    private final long timestamp;

    /**
     * 设备消息
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     */
    protected ThingMessage(String productId, String thingId, long timestamp) {
        this.productId = productId;
        this.thingId = thingId;
        this.timestamp = timestamp;
    }

    /**
     * 获取产品ID
     *
     * @return 产品ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public String getThingId() {
        return thingId;
    }

    /**
     * 获取消息时间戳
     *
     * @return 消息时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

}
