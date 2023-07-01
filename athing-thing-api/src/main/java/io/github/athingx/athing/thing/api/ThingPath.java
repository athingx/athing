package io.github.athingx.athing.thing.api;

import java.net.URI;

/**
 * 设备路径
 */
public class ThingPath {

    private final String productId;
    private final String thingId;
    private final String urn;
    private final URI uri;
    private final String _string;

    /**
     * 设备路径
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     */
    public ThingPath(String productId, String thingId) {
        this.productId = productId;
        this.thingId = thingId;
        this.urn = "%s/%s".formatted(productId, thingId);
        this.uri = URI.create("thing://%s/%s".formatted(productId, thingId));
        this._string = uri.toString();
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
     * 设备路径转换为URN
     * <p>
     * 格式：{productId}/{thingId}
     *
     * @return URN
     */
    public String toURN() {
        return urn;
    }

    /**
     * 设备路径转换为URI
     * <p>
     * 格式：thing://{productId}/{thingId}
     *
     * @return URI
     */
    public URI toURI() {
        return uri;
    }

    @Override
    public String toString() {
        return _string;
    }

}
