package io.github.athingx.athing.platform.api;

/**
 * 设备平台异常
 */
public class ThingPlatformException extends Exception {

    /**
     * 设备平台异常
     *
     * @param message 错误信息
     * @param cause   错误原因
     */
    public ThingPlatformException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 设备平台异常
     *
     * @param message 错误信息
     */
    public ThingPlatformException(String message) {
        super(message);
    }

}
