package io.github.athingx.athing.thing.api.op.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 操作请求
 *
 * @param token   操作令牌
 * @param version 请求版本
 * @param ext     系统扩展
 * @param method  请求方法
 * @param data    请求数据
 * @param <T>     请求数据类型
 */
public record OpRequest<T>(
        @SerializedName("id") String token,
        @SerializedName("version") String version,
        @SerializedName("method") String method,
        @SerializedName("sys") Ext ext,
        @SerializedName("params") T data
) implements OpData {

    /**
     * 构建操作请求
     *
     * @param token  操作令牌
     * @param method 请求方法
     * @param data   请求数据
     * @param <T>    请求数据类型
     * @return 操作请求
     */
    public static <T> OpRequest<T> of(String token, String method, T data) {
        return new OpRequest<>(token, "1.0", method, new Ext(), data);
    }

    /**
     * 系统扩展
     *
     * @param ack 是否云端应答
     */
    public record Ext(int ack) {

        /**
         * 云端返回响应
         */
        public static final int ACK = 1;

        /**
         * 云端不返回响应
         */
        public static final int NO_ACK = 0;

        /**
         * 构建系统扩展
         */
        public Ext() {
            this(ACK);
        }

    }

}
