package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * 操作绑定
 *
 * @param <V> 绑定类型
 */
public interface OpBinding<V> {

    /**
     * 过滤（异步）
     *
     * @param fn 过滤函数
     * @return 操作绑定
     */
    OpBinding<V> matchesAsync(BiFunction<String, ? super V, CompletableFuture<Boolean>> fn);

    /**
     * 过滤
     *
     * @param fn 过滤函数
     * @return 操作绑定
     */
    OpBinding<V> matches(BiPredicate<String, ? super V> fn);

    /**
     * 映射数据为其他类型（异步）
     *
     * @param fn  映射函数
     * @param <R> 映射结果类型
     * @return 操作绑定
     */
    <R> OpBinding<R> mapAsync(BiFunction<String, ? super V, CompletableFuture<R>> fn);

    /**
     * 映射数据为其他类型：
     * {@code V -> R}
     *
     * @param fn  映射函数
     * @param <R> 映射结果类型
     * @return 操作绑定
     */
    <R> OpBinding<R> map(BiFunction<String, ? super V, ? extends R> fn);

    /**
     * 绑定消费操作
     *
     * @param fn 消费函数
     * @return 操作绑定
     */
    CompletableFuture<OpBind> bind(BiConsumer<String, ? super V> fn);

    /**
     * 绑定呼叫操作
     *
     * @param opOption 操作选项
     * @param fn       操作应答映射函数：{@code V -> R extends OpData}
     * @param <P>      请求数据类型
     * @param <R>      应答数据类型
     * @return 操作绑定
     */
    <P extends OpData, R extends OpData> CompletableFuture<OpCall<P, R>> call(Option opOption, BiFunction<String, ? super V, ? extends R> fn);

    /**
     * 绑定呼叫操作
     *
     * @param fn  操作应答映射函数：{@code V -> R extends OpData}
     * @param <P> 请求数据类型
     * @param <R> 应答数据类型
     * @return 操作绑定
     */
    <P extends OpData, R extends OpData> CompletableFuture<OpCall<P, R>> call(BiFunction<String, ? super V, ? extends R> fn);

    /**
     * 操作选项
     */
    class Option {

        /**
         * 超时(毫秒)
         */
        private long timeoutMs = 1000 * 30L;

        /**
         * 获取超时时间
         *
         * @return 超时时间
         */
        public long getTimeoutMs() {
            return timeoutMs;
        }

        /**
         * 设置超时时间
         *
         * @param timeoutMs 超时时间
         * @return this
         */
        public Option setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

    }

}
