package io.github.athingx.athing.thing.api.op.domain;

import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.ThingCodes;

/**
 * 操作应答
 *
 * @param <T> 应答数据类型
 */
public class OpResponse<T> implements OpData, ThingCodes {

    @SerializedName("id")
    private final String token;

    @SerializedName("code")
    private final int code;

    @SerializedName("message")
    private final String desc;

    @SerializedName("data")
    private final T data;

    /**
     * 构造操作应答
     *
     * @param token 操作令牌
     * @param code  应答编码
     * @param desc  应答消息
     * @param data  应答数据
     */
    public OpResponse(String token, int code, String desc, T data) {
        this.token = token;
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    @Override
    public String getToken() {
        return token;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public T getData() {
        return data;
    }

    /**
     * 是否应答成功
     *
     * @return TRUE | FALSE
     */
    public boolean isOk() {
        return code == OK;
    }

    /**
     * 构建应答成功（带数据）
     *
     * @param token 操作令牌
     * @param data  应答数据
     * @param <T>   数据类型
     * @return 操作应答
     */
    public static <T> OpResponse<T> success(String token, T data) {
        return new OpResponse<>(token, OK, "success", data);
    }

    /**
     * 构建应答成功（不带数据）
     *
     * @param token 操作令牌
     * @param desc  应答消息
     * @return 操作应答
     */
    public static OpResponse<Void> succeed(String token, String desc) {
        return new OpResponse<>(token, OK, desc, null);
    }

    /**
     * 构建应答成功（不带数据）
     *
     * @param token 操作令牌
     * @return 操作应答
     */
    public static OpResponse<Void> succeed(String token) {
        return success(token, null);
    }

    /**
     * 构建应答
     *
     * @param token 操作令牌
     * @param code  应答编码
     * @param desc  应答消息
     * @return 操作应答
     */
    public static OpResponse<Void> of(String token, int code, String desc) {
        return new OpResponse<>(token, code, desc, null);
    }

    /**
     * 构建应答
     *
     * @param token 操作令牌
     * @param code  应答编码
     * @param desc  应答消息
     * @param data  应答数据
     * @return 操作应答
     */
    public static <T> OpResponse<T> of(String token, int code, String desc, T data) {
        return new OpResponse<>(token, code, desc, data);
    }

}
