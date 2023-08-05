package io.github.athingx.athing.common;

/**
 * 设备端应答编码
 */
public interface ThingCodes {

    /**
     * 应答成功
     */
    int OK = 200;

    /**
     * 请求错误
     */
    int REQUEST_ERROR = 400;

    /**
     * 请求参数错误
     */
    int REQUEST_PARAMETER_ERROR = 460;

    /**
     * 请求过于频繁
     */
    int TOO_MANY_REQUESTS = 429;

}
