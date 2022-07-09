package io.github.athingx.athing.thing.api;

/**
 * 设备异常
 */
public class ThingException extends Exception {

    private final ThingPath path;

    /**
     * 设备异常
     *
     * @param thing   设备
     * @param message 错误信息
     * @param cause   错误异常
     */
    public ThingException(Thing thing, String message, Throwable cause) {
        this(thing.path(), message, cause);
    }

    /**
     * 设备异常
     *
     * @param thing   设备
     * @param message 错误信息
     */
    public ThingException(Thing thing, String message) {
        this(thing.path(), message);
    }

    /**
     * 设备异常
     *
     * @param path    设备路径
     * @param message 错误信息
     * @param cause   错误异常
     */
    public ThingException(ThingPath path, String message, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    /**
     * 设备异常
     *
     * @param path    设备路径
     * @param message 错误信息
     */
    public ThingException(ThingPath path, String message) {
        super(message);
        this.path = path;
    }

    @Override
    public String getLocalizedMessage() {
        return "%s occur error：%s".formatted(path, getMessage());
    }

    /**
     * 获取设备路径
     *
     * @return 设备路径
     */
    public ThingPath path() {
        return path;
    }

}
