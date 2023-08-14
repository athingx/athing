package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 设备操作调用
 *
 * @param <T> 请求类型
 * @param <R> 应答类型
 */
public interface ThingOpCaller<T, R> extends ThingOpBinder {

    /**
     * 调用
     *
     * @param data 请求数据
     * @return 应答数据
     */
    CompletableFuture<R> call(T data);

    /**
     * 调用前置转换
     *
     * @param before 前置转换函数，将入参从V转换为T
     * @param <V>    前置转换入参类型
     * @return 转换后的调用器
     */
    default <V> ThingOpCaller<V, R> compose(Function<? super V, ? extends T> before) {
        return new ThingOpCaller<>() {
            @Override
            public CompletableFuture<R> call(V data) {
                return ThingOpCaller.this.call(before.apply(data));
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return ThingOpCaller.this.unbind();
            }

        };
    }

    /**
     * 调用后置转换
     *
     * @param after 后置转换函数，将出参从R转换为V
     * @param <V>   后置转换出参类型
     * @return 转换后的调用器
     */
    default <V> ThingOpCaller<T, V> then(Function<? super R, ? extends V> after) {
        return new ThingOpCaller<>() {
            @Override
            public CompletableFuture<V> call(T data) {
                return ThingOpCaller.this.call(data).thenApply(after);
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return ThingOpCaller.this.unbind();
            }

        };
    }

}
