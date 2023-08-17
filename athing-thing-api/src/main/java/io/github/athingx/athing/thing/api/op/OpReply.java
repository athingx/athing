package io.github.athingx.athing.thing.api.op;

import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.ThingCodes;

import java.util.function.Function;

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
     * 处理数据
     *
     * @param handler 处理器
     * @param <R>     处理结果类型
     * @return 处理结果
     */
    public <R> R handle(Function<T, R> handler) {
        if (!isSuccess()) {
            throw new OpReplyException(token, code, desc);
        }
        return handler.apply(data);
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
    public static <T> OpReply<T> fail(String token, int code, String desc) {
        return new OpReply<>(token, code, desc, null);
    }

    /**
     * 构造失败应答
     *
     * @param cause 异常
     * @param <T>   应答数据类型
     * @return 应答
     */
    public static <T> OpReply<T> fail(OpReplyException cause) {
        return fail(cause.getToken(), cause.getCode(), cause.getMessage());
    }

}
