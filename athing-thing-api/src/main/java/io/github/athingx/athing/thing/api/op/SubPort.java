package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpDecoder;

import static io.github.athingx.athing.thing.api.op.OpQos.AT_LEAST_ONCE;

/**
 * 订阅端口
 * <p>
 * 通过订阅端口从服务器上接收数据
 *
 * @param <V> 数据类型
 */
public record SubPort<V>(OpQos qos, String express, OpDecoder<byte[], V> decoder) {

    /**
     * 解码订阅数据
     *
     * @param decoder 解码器
     * @param <T>     解码数据类型
     * @return 订阅端口
     */
    public <T> SubPort<T> decode(OpDecoder<? super V, ? extends T> decoder) {
        return new SubPort<>(qos, express, this.decoder.then(decoder));
    }

    /**
     * 构造订阅端口
     *
     * @param qos     QOS
     * @param express 订阅表达式
     * @return 订阅端口
     */
    public static SubPort<byte[]> express(OpQos qos, String express) {
        return new SubPort<>(qos, express, OpDecoder.identity());
    }

    /**
     * 构造订阅端口
     *
     * @param express 订阅表达式
     * @return 订阅端口
     */
    public static SubPort<byte[]> express(String express) {
        return express(AT_LEAST_ONCE, express);
    }

}
