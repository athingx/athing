package io.github.athingx.athing.thing.api.op;

import java.util.function.BiFunction;

/**
 * 发布端口
 * <p>
 * 通过发布端口发布数据到服务器
 *
 * @param <V> 数据类型
 */
public class PubPort<V extends OpData> {

    private final int qos;
    private final String pattern;
    private final BiFunction<String, ? super V, String> formatter;

    /**
     * 发布端口
     *
     * @param qos       QOS
     * @param pattern   发布主题模板
     * @param formatter 发布主题格式化器
     */
    public PubPort(int qos, String pattern, BiFunction<String, ? super V, String> formatter) {
        this.qos = qos;
        this.pattern = pattern;
        this.formatter = formatter;
    }

    /**
     * 发布端口
     *
     * @param pattern   发布主题模板
     * @param formatter 发布主题格式化器
     */
    public PubPort(String pattern, BiFunction<String, ? super V, String> formatter) {
        this(1, pattern, formatter);
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

    /**
     * 操作投递构建器
     *
     * @param topic 投递主题
     * @param <V>   投递数据类型
     * @return 操作投递构建器
     */
    public static <V extends OpData> PubPort<V> topic(String topic) {
        return new PubPort<>(topic, (p, v) -> p);
    }

    /**
     * 操作投递构建器
     *
     * @param pattern   主题表达式
     * @param formatter 主题格式化器
     * @param <V>       投递数据类型
     * @return 操作投递构建器
     */
    public static <V extends OpData> PubPort<V> topic(String pattern, BiFunction<String, ? super V, String> formatter) {
        return new PubPort<>(pattern, formatter);
    }

    /**
     * 获取QOS
     *
     * @return QOS
     */
    public int qos() {
        return qos;
    }

}
