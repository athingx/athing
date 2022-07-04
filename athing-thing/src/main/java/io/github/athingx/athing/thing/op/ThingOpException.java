package io.github.athingx.athing.thing.op;

import io.github.athingx.athing.thing.Thing;
import io.github.athingx.athing.thing.ThingException;
import io.github.athingx.athing.thing.ThingPath;

/**
 * 设备操作异常
 */
public class ThingOpException extends ThingException {

    private final int token;

    /**
     * 设备操作异常
     *
     * @param thing   设备
     * @param token   操作令牌
     * @param message 错误消息
     * @param cause   错误异常
     */
    public ThingOpException(Thing thing, int token, String message, Throwable cause) {
        super(thing, message, cause);
        this.token = token;
    }

    /**
     * 设备操作异常
     *
     * @param thing   设备
     * @param token   操作令牌
     * @param message 错误消息
     */
    public ThingOpException(Thing thing, int token, String message) {
        super(thing, message);
        this.token = token;
    }

    /**
     * 设备操作异常
     *
     * @param path    设备路径
     * @param token   操作令牌
     * @param message 错误消息
     */
    public ThingOpException(ThingPath path, int token, String message, Throwable cause) {
        super(path, message, cause);
        this.token = token;
    }

    /**
     * 设备操作异常
     *
     * @param path    设备路径
     * @param token   操作令牌
     * @param message 错误消息
     */
    public ThingOpException(ThingPath path, int token, String message) {
        super(path, message);
        this.token = token;
    }

    /**
     * 获取操作令牌
     *
     * @return 操作令牌
     */
    public int token() {
        return token;
    }

    @Override
    public String getLocalizedMessage() {
        return "%s occur error: token=%s;message=%s".formatted(path(), token(), getMessage());
    }

}
