package io.github.athingx.athing.thing;

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

    public String getProductId() {
        return productId;
    }

    public String getThingId() {
        return thingId;
    }

    public String toURN() {
        return urn;
    }

    public URI toURI() {
        return uri;
    }

    @Override
    public String toString() {
        return _string;
    }

}
