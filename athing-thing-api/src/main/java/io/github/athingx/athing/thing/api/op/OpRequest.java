package io.github.athingx.athing.thing.api.op;

import static io.github.athingx.athing.thing.api.op.OpRequest.Ext.ACK;

/**
 * 操作请求
 *
 * @param token   操作令牌
 * @param version 版本
 * @param method  方法
 * @param ext     扩展
 * @param params  参数
 * @param <T>     参数类型
 */
public record OpRequest<T>(String token, String version, String method, Ext ext, T params) implements OpData {

    public static final String DEFAULT_VERSION = "1.0";

    /**
     * 构造操作请求
     *
     * @param token  令牌
     * @param method 方法
     * @param params 参数
     */
    public OpRequest(String token, String method, T params) {
        this(token, DEFAULT_VERSION, method, new Ext(ACK), params);
    }

    /**
     * 构造操作请求
     *
     * @param token  令牌
     * @param params 参数
     */
    public OpRequest(String token, T params) {
        this(token, DEFAULT_VERSION, null, new Ext(ACK), params);
    }


    /**
     * 操作请求扩展
     *
     * @param ack 是否需要应答
     */
    public record Ext(int ack) {

        /**
         * 无需应答
         */
        public static final int NO_ACK = 0;

        /**
         * 需要应答
         */
        public static final int ACK = 1;

    }

}
