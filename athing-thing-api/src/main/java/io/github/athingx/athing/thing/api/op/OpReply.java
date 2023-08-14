package io.github.athingx.athing.thing.api.op;

import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.ThingCodes;

/**
 * 操作应答
 *
 * @param token 操作令牌
 * @param code  应答编码
 * @param desc  应答消息
 * @param data  应答数据
 * @param <T>   应答数据类型
 */
public record OpReply<T>(
        @SerializedName("id") String token,
        @SerializedName("code") int code,
        @SerializedName("message") String desc,
        @SerializedName("data") T data
) implements OpData, ThingCodes {

    /**
     * 是否成功
     *
     * @return TRUE | FALSE
     */
    public boolean isSuccess() {
        return code == OK;
    }

    /**
     * 构造成功应答
     *
     * @param token 操作令牌
     * @param data  应答数据
     * @param desc  应答消息
     * @param <T>   应答数据类型
     * @return 应答
     */
    public static <T> OpReply<T> succeed(String token, T data, String desc) {
        return new OpReply<>(token, OK, desc, data);
    }

    /**
     * 构造成功应答
     *
     * @param token 操作令牌
     * @param data  应答数据
     * @param <T>   应答数据类型
     * @return 应答
     */
    public static <T> OpReply<T> succeed(String token, T data) {
        return succeed(token, data, "SUCCESS");
    }

    /**
     * 构造成功应答
     *
     * @param token 操作令牌
     * @param <T>   应答数据类型
     * @return 应答
     */
    public static <T> OpReply<T> succeed(String token) {
        return succeed(token, null, "SUCCESS");
    }

    /**
     * 构造失败应答
     *
     * @param token 操作令牌
     * @param code  应答编码
     * @param desc  应答消息
     * @param <T>   应答数据类型
     * @return 应答
     */
    public static <T> OpReply<T> failure(String token, int code, String desc) {
        return new OpReply<>(token, code, desc, null);
    }

    /**
     * 构造失败应答
     *
     * @param cause 异常
     * @param <T>   应答数据类型
     * @return 应答
     */
    public static <T> OpReply<T> failure(OpReplyException cause) {
        return failure(cause.getToken(), cause.getCode(), cause.getMessage());
    }

}
