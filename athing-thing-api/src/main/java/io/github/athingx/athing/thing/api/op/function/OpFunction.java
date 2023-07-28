package io.github.athingx.athing.thing.api.op.function;

/**
 * 操作函数
 *
 * @param <T> 操作数据类型
 * @param <R> 操作返回数据类型
 */
@FunctionalInterface
public interface OpFunction<T, R> {

    /**
     * 操作
     *
     * @param topic 主题
     * @param token 操作令牌
     * @param data  操作数据
     * @return 操作返回数据
     */
    R apply(String topic, String token, T data);

}
