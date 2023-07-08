package io.github.athingx.athing.thing.api.op;

import java.util.function.BiFunction;

/**
 * 发布端口
 * <p>
 * 通过发布端口发布数据到服务器
 *
 * @param <V> 数据类型
 */
public class PubPort<V> {

    private final int qos;
    private final String pattern;
    private final BiFunction<String/*PATTERN*/, ? super V, String> formatter;
    private final BiFunction<String/*TOPIC*/, ? super V, ? extends OpData> encoder;

    /**
     * 发布端口
     *
     * @param qos       QOS
     * @param pattern   发布主题模板
     * @param formatter 发布主题格式化器
     */
    public PubPort(int qos, String pattern, BiFunction<String, ? super V, String> formatter, BiFunction<String, ? super V, ? extends OpData> encoder) {
        this.qos = qos;
        this.pattern = pattern;
        this.formatter = formatter;
        this.encoder = encoder;
    }

    /**
     * 投递主题
     * <p>
     * 一些发布主题是由数据动态生成的，比如：${productId}/${thingId}/property/${property}，
     * 所以需要通过投递数据来生成发布主题
     *
     * @param data 投递数据
     * @return 投递主题
     */
    public String topic(V data) {
        return formatter.apply(pattern, data);
    }

    public OpData encode(String token, V data) {
        return encoder.apply(token, data);
    }

    public static <V extends OpData> Builder<V, V> newBuilder() {
        return new Builder<>(1, (token, data) -> data);
    }

    /**
     * 获取QOS
     *
     * @return QOS
     */
    public int qos() {
        return qos;
    }

    public static class Builder<T, R> {

        private final int qos;
        private final BiFunction<String, ? super R, ? extends OpData> encoder;

        public Builder(int qos, BiFunction<String, ? super R, ? extends OpData> encoder) {
            this.qos = qos;
            this.encoder = encoder;
        }

        public Builder<T, R> qos(int qos) {
            return new Builder<>(qos, encoder);
        }

        public <V> Builder<R, V> encode(BiFunction<String, ? super V, ? extends R> encoder) {
            return new Builder<>(qos, (token, data) -> this.encoder.apply(token, encoder.apply(token, data)));
        }

        public <V> Builder<R, V> encode(Class<? super V> type, BiFunction<String, ? super V, ? extends R> encoder) {
            return encode(encoder);
        }

        public PubPort<R> build(String pattern, BiFunction<String, ? super R, String> formatter) {
            return new PubPort<>(qos, pattern, formatter, encoder);
        }

        public PubPort<R> build(String topic) {
            return new PubPort<>(qos, topic, (pattern, data) -> pattern, encoder);
        }

    }

}
