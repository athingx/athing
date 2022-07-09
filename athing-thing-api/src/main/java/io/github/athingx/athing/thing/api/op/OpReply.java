package io.github.athingx.athing.thing.api.op;

/**
 * 操作应答
 *
 * @param token 操作令牌
 * @param code  应答编码
 * @param desc  应答消息
 * @param data  应答数据
 * @param <T>   数据类型
 */
public record OpReply<T>(String token, int code, String desc, T data) implements OpData {

    /**
     * 成功应答：200
     */
    private static final int CODE_OK = 200;

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
