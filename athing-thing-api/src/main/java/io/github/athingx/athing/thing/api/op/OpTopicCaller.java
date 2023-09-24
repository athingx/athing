package io.github.athingx.athing.thing.api.op;

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
                return OpTopicCaller.this.call(topics.apply(t), t);
            }

            @Override
            public CompletableFuture<Void> unbind() {
                return OpTopicCaller.this.unbind();
            }
        };
    }

}
