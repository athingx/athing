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
public interface OpBind<V> {

    /**
     * 过滤
     *
     * @param matcher 过滤函数
     * @return 操作绑定
     */
    OpBind<V> matches(OpPredicate<? super V> matcher);

    /**
     * 映射数据为其他类型：
     * {@code V -> R}
     *
     * @param mapper 映射函数
     * @param <R>    映射结果类型
     * @return 操作绑定
     */
    <R> OpBind<R> map(OpFunction<? super V, ? extends R> mapper);

    /**
     * 绑定消费操作
     *
     * @param consumer 消费函数
     * @return 操作绑定
     */
    CompletableFuture<OpBinder> consumer(OpConsumer<? super V> consumer);

    /**
     * 绑定呼叫操作
     *
     * @param opOption 操作选项
     * @param mapper   操作应答映射函数：{@code V -> R extends OpData}
     * @param <P>      请求数据类型
     * @param <R>      应答数据类型
     * @return 操作绑定
     */
    <P extends OpData, R extends OpData>
    CompletableFuture<OpCaller<P, R>> caller(Option opOption, OpFunction<? super V, ? extends R> mapper);

    /**
     * 绑定呼叫操作
     *
     * @param mapper 操作应答映射函数：{@code V -> R extends OpData}
     * @param <P>    请求数据类型
     * @param <R>    应答数据类型
     * @return 操作绑定
     */
    default <P extends OpData, R extends OpData>
    CompletableFuture<OpCaller<P, R>> caller(OpFunction<? super V, ? extends R> mapper) {
        return caller(new Option(), mapper);
    }

    /**
     * 操作选项
     */
    class Option {

        /**
         * 超时(毫秒)
         */
        private long timeoutMs = 1000 * 30L;

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public Option setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

    }

}
