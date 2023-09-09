package io.github.athingx.athing.thing.api.op.function;

/**
 * 操作消费函数
 *
 * @param <V> 消费数据类型
 */
@FunctionalInterface
public interface OpConsumer<V> {

    /**
     * 消费数据
     *
     * @param topic 主题
     * @param data  数据
     */
    void accept(String topic, V data);

}
