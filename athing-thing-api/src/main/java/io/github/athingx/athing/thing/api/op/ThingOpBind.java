package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpConsumer;
import io.github.athingx.athing.thing.api.op.function.OpFunction;
import io.github.athingx.athing.thing.api.op.function.OpPredicate;

import java.util.concurrent.CompletableFuture;

/**
 * 操作绑定
 *
 * @param <V> 绑定类型
 */
public interface ThingOpBind<V> {

    /**
     * 过滤
     *
     * @param filter 过滤函数
     * @return 操作绑定
     */
    ThingOpBind<V> filter(OpPredicate<? super V> filter);

    /**
     * 映射数据为其他类型：
     * {@code V -> R}
     *
     * @param mapper 映射函数
     * @param <R>    映射结果类型
     * @return 操作绑定
     */
    <R> ThingOpBind<R> map(OpFunction<? super V, ? extends R> mapper);

    /**
     * 绑定消费操作
     *
     * @param consumer 消费函数
     * @return 操作绑定
     */
    CompletableFuture<ThingOpBinder> consumer(OpConsumer<? super V> consumer);

    /**
     * 绑定呼叫操作
     *
     * @param option 操作选项
     * @param mapper 操作应答映射函数：{@code V -> R extends OpData}
     * @param <P>    请求数据类型
     * @param <R>    应答数据类型
     * @return 操作绑定
     */
    <P extends OpData, R extends OpData>
    CompletableFuture<ThingOpRouteCaller<P, R>> caller(Option option, OpFunction<? super V, ? extends R> mapper);

    /**
     * 绑定呼叫操作
     * @param mapper 操作应答映射函数：{@code V -> R extends OpData}
     * @return 操作绑定
     * @param <P> 请求数据类型
     * @param <R> 应答数据类型
     */
    <P extends OpData, R extends OpData>
    CompletableFuture<ThingOpRouteCaller<P, R>> caller(OpFunction<? super V, ? extends R> mapper);

    /**
     * 操作选项
     */
    class Option {

        /**
         * 超时(毫秒)
         */
        private long timeoutMs = -1;

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public Option setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

    }

}
