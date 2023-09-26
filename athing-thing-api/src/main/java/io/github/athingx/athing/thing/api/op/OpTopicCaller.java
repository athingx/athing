package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.common.util.CompletableFutureUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 调用操作
 *
 * @param <T> 请求数据类型
 * @param <R> 应答数据类型
 */
public interface OpTopicCaller<T, R> extends OpBinder {

    /**
     * 调用
     *
     * @param topic 请求主题
     * @param t     请求数据
     * @return 应答数据
     */
    CompletableFuture<R> call(String topic, T t);

    /**
     * 主题路由（固定主题）
     *
     * @param topic 主题
     * @return 调用操作
     */
    default OpCaller<T, R> topics(String topic) {
        return topics(t -> topic);
    }

    default <U> OpTopicCaller<U, R> compose(Function<? super U, ? extends T> fn) {
        return new OpTopicCaller<>() {
            @Override
            public CompletableFuture<R> call(String topic, U u) {
                return CompletableFutureUtils
                        .supply(() -> fn.apply(u))
                        .thenCompose(t -> OpTopicCaller.this.call(topic, t));
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return OpTopicCaller.this.unbind();
            }
        };
    }

    default <U> OpTopicCaller<T, U> then(Function<? super R, ? extends U> fn) {
        return new OpTopicCaller<>() {
            @Override
            public CompletableFuture<U> call(String topic, T t) {
                return OpTopicCaller.this.call(topic, t).thenApply(fn);
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return OpTopicCaller.this.unbind();
            }
        };
    }

    /**
     * 主题路由
     *
     * @param topics 主题路由函数
     * @return 调用操作
     */
    default OpCaller<T, R> topics(Function<T, String> topics) {
        return new OpCaller<>() {
            @Override
            public CompletableFuture<R> call(T t) {
                return CompletableFutureUtils
                        .supply(() -> topics.apply(t))
                        .thenCompose(topic -> OpTopicCaller.this.call(topic, t));
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return OpTopicCaller.this.unbind();
            }
        };
    }

}
