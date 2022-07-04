package io.github.athingx.athing.thing.function;

import java.util.function.BiFunction;

/**
 * 映射函数
 *
 * @param <T> 原始类型
 * @param <R> 目标类型
 */
@FunctionalInterface
public interface ThingFnMap<T, R> extends BiFunction<String, T, R> {

    /**
     * 映射
     *
     * @param topic 消息主题
     * @param data  原始数据
     * @return 目标数据
     */
    @Override
    default R apply(String topic, T data) {
        return mapping(topic, data);
    }

    /**
     * 映射
     *
     * @param topic 消息主题
     * @param data  原始数据
     * @return 目标数据
     */
    R mapping(String topic, T data);

    /**
     * 自映射（{@code R -> R}）
     *
     * @param <R> 数据类型
     * @return 映射函数
     */
    static <R> ThingFnMap<R, R> identity() {
        return (topic, data) -> data;
    }

}
