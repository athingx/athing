package io.github.athingx.athing.thing.function;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * 过滤函数
 *
 * @param <T> 数据类型
 */
@FunctionalInterface
public interface ThingFnMatcher<T> extends BiPredicate<String, T> {

    /**
     * 过滤
     *
     * @param topic 主题
     * @param data  数据
     * @return TRUE | FALSE
     */
    @Override
    default boolean test(String topic, T data) {
        return matches(topic, data);
    }

    /**
     * 过滤
     *
     * @param topic 主题
     * @param data  数据
     * @return TRUE | FALSE
     */
    boolean matches(String topic, T data);

    /**
     * 主题过滤
     *
     * @param fn  主题过滤函数
     * @param <T> 数据类型
     * @return 过滤函数
     */
    static <T> ThingFnMatcher<T> matchesTopic(Predicate<String> fn) {
        return (topic, data) -> fn.test(topic);
    }

    /**
     * 主题过滤（正则）
     *
     * @param regex 正则表达式
     * @param <T>   数据类型
     * @return 过滤函数
     */
    static <T> ThingFnMatcher<T> matchesTopicByRegex(String regex) {
        return matchesTopic(topic -> topic.matches(regex));
    }

}
