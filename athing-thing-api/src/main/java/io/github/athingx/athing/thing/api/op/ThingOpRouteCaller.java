package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpFunction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 设备操作调用
 *
 * @param <T> 请求类型
 * @param <R> 应答类型
 */
public interface ThingOpRouteCaller<T, R> extends ThingOpBinder {

    /**
     * 调用
     *
     * @param topic 请求主题
     * @param data  请求数据
     * @return 应答结果
     */
    CompletableFuture<R> call(String topic, T data);

    /**
     * 调用前置转换
     *
     * @param before 前置转换函数，将入参从V转换为T
     * @param <V>    前置转换入参类型
     * @return 转换后的调用器
     */
    default <V> ThingOpRouteCaller<V, R> compose(OpFunction<? super V, ? extends T> before) {
        return new ThingOpRouteCaller<>() {
            @Override
            public CompletableFuture<R> call(String topic, V data) {
                return ThingOpRouteCaller.this.call(topic, before.apply(topic, data));
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return ThingOpRouteCaller.this.unbind();
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
    default <V> ThingOpRouteCaller<T, V> then(OpFunction<? super R, ? extends V> after) {
        return new ThingOpRouteCaller<>() {
            @Override
            public CompletableFuture<V> call(String topic, T data) {
                return ThingOpRouteCaller.this.call(topic, data).thenApply(r -> after.apply(topic, r));
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return ThingOpRouteCaller.this.unbind();
            }

        };
    }

    /**
     * 路由应答主题
     *
     * @param router 应答主题路由函数
     * @return 路由后的调用器
     */
    default ThingOpCaller<T, R> route(Function<? super T, String> router) {
        return new ThingOpCaller<>() {
            @Override
            public CompletableFuture<R> call(T data) {
                return ThingOpRouteCaller.this.call(router.apply(data), data);
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return ThingOpRouteCaller.this.unbind();
            }

        };
    }

}
