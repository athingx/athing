package io.github.athingx.athing.thing.api.op.function;

/**
 * 操作匹配函数
 * @param <V> 数据类型
 */
public interface OpPredicate<V> {

    /**
     * 匹配
     * @param topic 主题
     * @param data 数据
     * @return TRUE | FALSE
     */
    boolean test(String topic, V data);

}
