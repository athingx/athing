package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpEncoder;

import java.util.function.Function;

import static io.github.athingx.athing.thing.api.op.OpQos.AT_LEAST_ONCE;

/**
 * 发布端口
 * <p>
 * 通过发布端口发布数据到服务器
 *
 * @param <V> 数据类型
 */
public class PubPort<V> {

    private final OpQos qos;
    private final Function<V, String> formatter;
    private final OpEncoder<V, byte[]> encoder;

    public PubPort(OpQos qos, Function<V, String> formatter, OpEncoder<V, byte[]> encoder) {
        this.qos = qos;
        this.formatter = formatter;
        this.encoder = encoder;
    }

    public OpQos getQos() {
        return qos;
    }

    public String topic(V data) {
        return formatter.apply(data);
    }

    public byte[] encode(String token, V data) {
        return encoder.encode(token, data);
    }

    public static Builder<byte[]> newBuilder(OpQos qos) {
        return new Builder<>(qos, OpEncoder.identity());
    }

    public static Builder<byte[]> newBuilder() {
        return newBuilder(AT_LEAST_ONCE);
    }

    public static class Builder<V> {

        private final OpQos qos;
        private final OpEncoder<V, byte[]> encoder;

        public Builder(OpQos qos, OpEncoder<V, byte[]> encoder) {
            this.qos = qos;
            this.encoder = encoder;
        }

        public <T> Builder<T> encode(OpEncoder<? super T, ? extends V> encoder) {
            return new Builder<>(qos, this.encoder.compose(encoder));
        }

        public PubPort<V> build(String topic) {
            return new PubPort<>(qos, v -> topic, encoder);
        }

        public PubPort<V> build(Function<V, String> formatter) {
            return new PubPort<>(qos, formatter, encoder);
        }

    }

}
