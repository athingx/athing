package io.github.athingx.athing.thing.builder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

import static io.github.athingx.athing.thing.impl.util.StringUtils.bytesToHexString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备接入
 */
public class ThingAccess {

    private final String productId;
    private final String thingId;
    private final String secret;
    private final String uniqueId = UUID.randomUUID().toString();
    private final long timestamp = System.currentTimeMillis();

    public ThingAccess(String productId, String thingId, String secret) {
        this.productId = productId;
        this.thingId = thingId;
        this.secret = secret;
    }

    public String getProductId() {
        return productId;
    }

    public String getThingId() {
        return thingId;
    }

    /**
     * 获取MQTT帐号
     *
     * @return MQTT帐号
     */
    public String getUsername() {
        return "%s&%s".formatted(thingId, productId);
    }

    /**
     * 获取MQTT密码
     *
     * @return MQTT密码
     */
    public char[] getPassword() {
        final String content = "clientId%sdeviceName%sproductKey%stimestamp%s".formatted(
                uniqueId,
                thingId,
                productId,
                timestamp
        );
        try {
            final Mac mac = Mac.getInstance("HMACSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(UTF_8), mac.getAlgorithm()));
            return bytesToHexString(mac.doFinal(content.getBytes(UTF_8))).toCharArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取MQTT客户端ID
     *
     * @return 客户端ID
     */
    public String getClientId() {
        return "%s|securemode=3,signmethod=hmacsha1,timestamp=%s,ext=1|".formatted(uniqueId, timestamp);
    }

}
