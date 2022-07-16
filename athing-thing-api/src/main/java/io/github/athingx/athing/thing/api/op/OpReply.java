package io.github.athingx.athing.thing.api.op;

import com.google.gson.annotations.SerializedName;

/**
 * 操作应答
 *
 * @param <T> 数据类型
 */
public class OpReply<T> implements OpData {

    /**
     * 成功应答：200
     */
    private static final int CODE_OK = 200;

    @SerializedName("id")
    private final String token;

    @SerializedName("code")
    private final int code;

    @SerializedName("message")
    private final String desc;

    @SerializedName("data")
    private final T data;

    /**
     * 操作应答
     *
     * @param token 操作令牌
     * @param code  应答编码
     * @param desc  应答消息
     * @param data  应答数据
     */
    private OpReply(String token, int code, String desc, T data) {
        this.token = token;
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    @Override
    public String token() {
        return token;
    }

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }

    public T data() {
        return data;
    }


    /**
     * 是否应答成功
     *
     * @return TRUE | FALSE
     */
    public boolean isOk() {
        return code == CODE_OK;
    }

    /**
     * 构建应答成功（带数据）
     *
     * @param token 操作令牌
     * @param data  应答数据
     * @param <T>   数据类型
     * @return 操作应答
     */
    public static <T> OpReply<T> success(String token, T data) {
        return new OpReply<>(token, CODE_OK, "success", data);
    }

    /**
     * 构建应答成功（不带数据）
     *
     * @param token 操作令牌
     * @param desc  应答消息
     * @return 操作应答
     */
    public static OpReply<Void> success(String token, String desc) {
        return new OpReply<>(token, CODE_OK, desc, null);
    }

    /**
     * 构建应答成功（不带数据）
     *
     * @param token 操作令牌
     * @return 操作应答
     */
    public static OpReply<Void> success(String token) {
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
    public static OpReply<Void> reply(String token, int code, String desc) {
        return new OpReply<>(token, code, desc, null);
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
    public static <T> OpReply<T> reply(String token, int code, String desc, T data) {
        return new OpReply<>(token, code, desc, data);
    }


}
