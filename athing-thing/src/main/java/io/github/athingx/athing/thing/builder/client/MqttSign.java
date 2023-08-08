package io.github.athingx.athing.thing.builder.client;

import io.github.athingx.athing.thing.api.ThingPath;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

import static io.github.athingx.athing.thing.impl.util.StringUtils.bytesToHexString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 阿里云MQTT签名
 */
public class MqttSign {

    final String uniqueId = UUID.randomUUID().toString();
    final long timestamp = System.currentTimeMillis();
    final ThingPath path;

    /**
     * 构建启动信息
     *
     * @param path 设备路径
     */
    MqttSign(ThingPath path) {
        this.path = path;
    }

    /**
     * 获取MQTT帐号
     *
     * @return MQTT帐号
     */
    String getUsername() {
        return "%s&%s".formatted(path.getThingId(), path.getProductId());
    }

    /**
     * 获取MQTT密码
     *
     * @return MQTT密码
     */
    char[] getPassword(String secret) {
        final String content = "clientId%sdeviceName%sproductKey%stimestamp%s".formatted(
                uniqueId,
                path.getThingId(),
                path.getProductId(),
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
    String getClientId() {
        return "%s|securemode=3,signmethod=hmacsha1,timestamp=%s,ext=1|".formatted(uniqueId, timestamp);
    }

}
