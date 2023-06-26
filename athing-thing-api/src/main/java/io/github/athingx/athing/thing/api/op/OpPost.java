package io.github.athingx.athing.thing.api.op;

import java.util.function.BiFunction;

/**
 * 操作投递
 *
 * @param <V> 投递数据类型
 */
public class OpPost<V extends OpData> {

    private final String pattern;
    private final BiFunction<String, ? super V, String> formatter;

    /**
     * 操作投递
     *
     * @param pattern   主题表达式
     * @param formatter 主题格式化器
     */
    public OpPost(String pattern, BiFunction<String, ? super V, String> formatter) {
        this.pattern = pattern;
        this.formatter = formatter;
    }

    /**
     * 投递主题
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
    public static <V extends OpData> OpPost<V> topic(String topic) {
        return new OpPost<>(topic, (p, v) -> p);
    }

    /**
     * 操作投递构建器
     *
     * @param pattern   主题表达式
     * @param formatter 主题格式化器
     * @param <V>       投递数据类型
     * @return 操作投递构建器
     */
    public static <V extends OpData> OpPost<V> topic(String pattern, BiFunction<String, ? super V, String> formatter) {
        return new OpPost<>(pattern, formatter);
    }

}
