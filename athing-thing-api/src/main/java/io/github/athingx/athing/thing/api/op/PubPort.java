package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpEncoder;

import static io.github.athingx.athing.thing.api.op.OpQos.AT_LEAST_ONCE;

/**
 * 发布端口
 * <p>
 * 通过发布端口发布数据到服务器
 *
 * @param <V> 数据类型
 */
public record PubPort<V>(OpQos qos, String topic, OpEncoder<V, byte[]> encoder) {

    /**
     * 编码发布数据
     *
     * @param encoder 编码器
     * @param <T>     编码数据类型
     * @return 发布端口
     */
    public <T> PubPort<T> encode(OpEncoder<? super T, ? extends V> encoder) {
        return new PubPort<>(qos, topic, this.encoder.compose(encoder));
    }

    /**
     * 构造发布端口
     *
     * @param qos   QOS
     * @param topic 主题
     * @return 发布端口
     */
    public static PubPort<byte[]> topic(OpQos qos, String topic) {
        return new PubPort<>(qos, topic, OpEncoder.identity());
    }

    /**
     * 构造发布端口
     *
     * @param topic 主题
     * @return 发布端口
     */
    public static PubPort<byte[]> topic(String topic) {
        return topic(AT_LEAST_ONCE, topic);
    }

}
