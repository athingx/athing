package io.github.athingx.athing.thing.api.op;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 操作绑定
 */
public class OpBind<V> {

    private final String express;
    private final BiFunction<String, byte[], ? extends V> decoder;

    /**
     * 操作绑定
     *
     * @param express 绑定主题表达式
     * @param decoder 解码器
     */
    public OpBind(String express, BiFunction<String, byte[], ? extends V> decoder) {
        this.express = express;
        this.decoder = decoder;
    }

    /**
     * 绑定主题表达式
     *
     * @return 绑定主题表达式
     */
    public String express() {
        return express;
    }

    /**
     * 解码
     *
     * @param topic 消息主题
     * @param data  消息数据
     * @return 解码结果
     */
    public V decode(String topic, byte[] data) {
        return decoder.apply(topic, data);
    }

    /**
     * 创建操作绑定构建器
     *
     * @param express 绑定主题表达式
     * @return 操作绑定构建器
     */
    public static Builder<byte[], byte[]> newBuilder(String express) {
        return new Builder<>(express, (topic, data) -> data);
    }

    /**
     * 操作绑定构建器
     *
     * @param <T> 消息数据类型
     * @param <R> 解码结果类型
     */
    public static class Builder<T, R> {

        private final String express;
        private final BiFunction<String, byte[], ? extends R> decoder;

        /**
         * 操作绑定构建器
         *
         * @param express 绑定主题表达式
         * @param decoder 解码器
         */
        Builder(String express, BiFunction<String, byte[], ? extends R> decoder) {
            this.express = express;
            this.decoder = decoder;
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
        public <V> Builder<T, V> decode(Function<? super R, ? extends V> decoder) {
            return decode((topic, data) -> decoder.apply(data));
        }

        /**
         * 解码数据
         *
         * @param decoder 解码器
         * @param <V>     解码结果类型
         * @return this
         */
        public <V> Builder<T, V> decode(BiFunction<String, ? super R, ? extends V> decoder) {
            return new Builder<>(express, (topic, data) -> decoder.apply(topic, Builder.this.decoder.apply(topic, data)));
        }

        /**
         * 构建操作绑定
         *
         * @return 操作绑定
         */
        public OpBind<R> build() {
            return new OpBind<>(express, decoder);
        }

    }

}
