package io.github.athingx.athing.thing.api.op.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 操作请求
 *
 * @param <T> 请求数据类型
 */
public class OpRequest<T> implements OpData {


    @SerializedName("id")
    private final String token;
    @SerializedName("version")
    private final String version;
    @SerializedName("method")
    private final String method;
    @SerializedName("sys")
    private final Ext ext;
    @SerializedName("params")
    private final T data;

    /**
     * 构建操作请求
     *
     * @param token   操作令牌
     * @param version 请求版本
     * @param ext     系统扩展
     * @param method  请求方法
     * @param data    请求数据
     */
    public OpRequest(String token, String version, String method, Ext ext, T data) {
        this.token = token;
        this.version = version;
        this.method = method;
        this.ext = ext;
        this.data = data;
    }

    @Override
    public String getToken() {
        return token;
    }

    public String getVersion() {
        return version;
    }

    public String getMethod() {
        return method;
    }

    public Ext getExt() {
        return ext;
    }

    public T getData() {
        return data;
    }

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
     */
    public static class Ext {

        /**
         * 云端返回响应
         */
        public static final int ACK = 1;

        /**
         * 云端不返回响应
         */
        public static final int NO_ACK = 0;

        private final int ack;

        /**
         * 构建系统扩展
         *
         * @param ack 是否云端应答
         */
        public Ext(int ack) {
            this.ack = ack;
        }

        public Ext() {
            this.ack = ACK;
        }

        public int getAck() {
            return ack;
        }

    }

}
