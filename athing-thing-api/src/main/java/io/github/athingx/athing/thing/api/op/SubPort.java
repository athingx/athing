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
public class SubPort<V> {

    private final OpQos qos;
    private final String express;
    private final OpDecoder<byte[], V> decoder;

    public SubPort(OpQos qos, String express, OpDecoder<byte[], V> decoder) {
        this.qos = qos;
        this.express = express;
        this.decoder = decoder;
    }

    public OpQos getQos() {
        return qos;
    }

    public String getExpress() {
        return express;
    }

    public V decode(String token, byte[] data) {
        return decoder.decode(token, data);
    }

    public static Builder<byte[]> newBuilder(OpQos qos) {
        return new Builder<>(qos, OpDecoder.identity());
    }

    public static Builder<byte[]> newBuilder() {
        return newBuilder(AT_LEAST_ONCE);
    }

    public static class Builder<V> {

        private final OpQos qos;
        private final OpDecoder<byte[], V> decoder;

        public Builder(OpQos qos, OpDecoder<byte[], V> decoder) {
            this.qos = qos;
            this.decoder = decoder;
        }

        public <T> Builder<T> decode(OpDecoder<? super V, ? extends T> decoder) {
            return new Builder<>(qos, this.decoder.then(decoder));
        }

        public SubPort<V> build(String express) {
            return new SubPort<>(qos, express, decoder);
        }

    }

}
