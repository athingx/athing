package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpFunction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 设备呼叫绑定
 *
 * @param <T> 请求数据类型
 * @param <R> 应答数据类型
 */
public interface ThingOpRouteCaller<T, R> extends ThingOpBinder {

    /**
     * 呼叫
     *
     * @param topic 请求主题
     * @param data  请求数据
     * @return 应答结果
     */
    CompletableFuture<R> call(String topic, T data);

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
