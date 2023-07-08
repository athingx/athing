package io.github.athingx.athing.thing.api.op;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 订阅端口
 * <p>
 * 通过订阅端口从服务器上接收数据
 */
public class SubPort<V> {

    private final int qos;
    private final String express;
    private final BiFunction<String, byte[], V> decoder;

    /**
     * 订阅端口
     *
     * @param qos     QOS
     * @param express 订阅主题表达式
     * @param decoder 订阅数据解码器
     */
    public SubPort(int qos, String express, BiFunction<String, byte[], V> decoder) {
        this.qos = qos;
        this.express = express;
        this.decoder = decoder;
    }

    /**
     * 订阅端口
     *
     * @param express 订阅主题表达式
     * @param decoder 订阅数据解码器
     */
    public SubPort(String express, BiFunction<String, byte[], V> decoder) {
        this(1, express, decoder);
    }

    /**
     * 订阅主题表达式
     *
     * @return 订阅主题表达式
     */
    public String express() {
        return express;
    }

    /**
     * 获取QOS
     *
     * @return QOS
     */
    public int qos() {
        return qos;
    }

    /**
     * 接收数据解码
     * <p>
     * 从服务器端接收到的数据是二进制数据，需要通过解码器解码成具体的数据类型
     *
     * @param topic 接收主题
     * @param data  接收数据
     * @return 解码结果
     */
    public V decode(String topic, byte[] data) {
        return decoder.apply(topic, data);
    }

    /**
     * 创建操作绑定构建器
     *
     * @param express 绑定表达式
     * @return 操作绑定构建器
     */
    public static Builder<byte[], byte[]> newBuilder(String express) {
        return new Builder<>(1, express, (topic, data) -> data);
    }

    /**
     * 订阅端口构建器
     *
     * @param <T> 解码前数据类型
     * @param <R> 解码后数据类型
     */
    public static class Builder<T, R> {

        private final int qos;
        private final String express;
        private final BiFunction<String, byte[], R> decoder;

        /**
         * 操作绑定构建器
         *
         * @param qos     QOS
         * @param express 绑定表达式
         * @param decoder 解码器
         */
        Builder(int qos, String express, BiFunction<String, byte[], R> decoder) {
            this.qos = qos;
            this.express = express;
            this.decoder = decoder;
        }

        /**
         * 设置QOS
         *
         * @param qos QOS
         * @return this
         */
        public Builder<T, R> qos(int qos) {
            return new Builder<>(qos, express, decoder);
        }

        /**
         * 过滤数据
         *
         * @param filter 数据过滤器
         * @return this
         */
        public Builder<T, R> filter(BiPredicate<String, R> filter) {
            return decode((topic, data) -> filter.test(topic, data) ? data : null);
        }

        /**
         * 解码数据
         *
         * @param decoder 解码器
         * @param <V>     解码结果类型
         * @return this
         */
        public <V> Builder<T, V> decode(Function<R, V> decoder) {
            return decode((topic, data) -> decoder.apply(data));
        }

        /**
         * 解码数据
         *
         * @param decoder 解码器
         * @param <V>     解码结果类型
         * @return this
         */
        public <V> Builder<T, V> decode(BiFunction<String, R, V> decoder) {
            return new Builder<>(qos, express, (topic, data) -> decoder.apply(topic, Builder.this.decoder.apply(topic, data)));
        }

        /**
         * 构建订阅端口
         *
         * @return 订阅端口
         */
        public SubPort<R> build() {
            return new SubPort<>(qos, express, decoder);
        }

    }

}
