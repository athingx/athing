package io.github.athingx.athing.thing.api.op;

/**
 * 应答异常
 */
public class OpReplyException extends RuntimeException {

    private final String token;
    private final int code;

    /**
     * 应答异常
     * @param token 操作令牌
     * @param code 应答编码
     * @param desc 应答消息
     */
    public OpReplyException(String token, int code, String desc) {
        super(desc);
        this.token = token;
        this.code = code;
    }

    /**
     * 获取操作令牌
     * @return 操作令牌
     */
    public String getToken() {
        return token;
    }

    /**
     * 获取应答编码
     * @return 应答编码
     */
    public int getCode() {
        return code;
    }

}
