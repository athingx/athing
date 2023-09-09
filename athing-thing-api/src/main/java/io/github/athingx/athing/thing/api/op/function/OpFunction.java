package io.github.athingx.athing.thing.api.op.function;

import java.util.Objects;

/**
 * 操作函数
 *
 * @param <T> 请求类型
 * @param <R> 返回类型
 */
@FunctionalInterface
public interface OpFunction<T, R> {

    /**
     * 执行操作
     *
     * @param topic 主题
     * @param data  请求
     * @return 返回
     */
    R apply(String topic, T data);

    /**
     * 返回一个恒等函数
     *
     * @param <V> 类型
     * @return {@code v->v}
     */
    static <V> OpFunction<V, V> identity() {
        return (topic, v) -> v;
    }

    /**
     * 组合前驱函数，先执行前驱函数，再执行当前函数
     *
     * @param before 前驱函数
     * @param <V>    前驱类型
     * @return 组合函数
     */
    default <V> OpFunction<V, R> compose(OpFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (topic, v) -> apply(topic, before.apply(topic, v));
    }

    /**
     * 组合后驱函数，先执行当前函数，再执行后驱函数
     *
     * @param after 后驱函数
     * @param <V>   后驱类型
     * @return 组合函数
     */
    default <V> OpFunction<T, V> then(OpFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (topic, t) -> after.apply(topic, apply(topic, t));
    }

}
