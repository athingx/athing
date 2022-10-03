package io.github.athingx.athing.platform.api.message;

import io.github.athingx.athing.common.ThingCodes;

/**
 * 设备应答消息
 */
public class ThingReplyMessage extends ThingMessage implements ThingCodes {

    private final String token;
    private final int code;
    private final String desc;

    /**
     * 设备应答消息
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     * @param token     请求令牌
     * @param code      应答码
     * @param desc      应答描述
     */
    protected ThingReplyMessage(
            String productId, String thingId, long timestamp,
            String token, int code, String desc
    ) {
        super(productId, thingId, timestamp);
        this.token = token;
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取请求令牌
     *
     * @return 请求令牌
     */
    public String getToken() {
        return token;
    }

    /**
     * 判断应答结果是否成功
     *
     * @return TRUE | FALSE
     */
    public boolean isOK() {
        return getCode() == OK;
    }

    /**
     * 获取应答码
     *
     * @return 应答码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取应答描述
     *
     * @return 应答描述
     */
    public String getDesc() {
        return desc;
    }

}
