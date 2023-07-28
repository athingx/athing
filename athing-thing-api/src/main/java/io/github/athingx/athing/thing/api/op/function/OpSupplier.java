package io.github.athingx.athing.thing.api.op.function;

/**
 * 操作数据供应者
 *
 * @param <V>
 */
@FunctionalInterface
public interface OpSupplier<V> {

    /**
     * 获取数据
     *
     * @param topic 主题
     * @param token 操作令牌
     * @return 数据
     */
    V get(String topic, String token);

}
