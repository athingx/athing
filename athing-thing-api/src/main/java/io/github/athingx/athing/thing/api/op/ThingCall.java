package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 设备数据调用者
 *
 * @param <T> 请求数据类型
 * @param <R> 应答数据类型
 */
public interface ThingCall<T, R> extends ThingBind {

    /**
     * 调用
     *
     * @param data 请求数据
     * @return 应答结果
     */
    default CompletableFuture<R> call(T data) {
        return call(new Option(), data);
    }

    /**
     * 调用
     *
     * @param option 调用选项
     * @param data   请求数据
     * @return 应答结果
     */
    CompletableFuture<R> call(Option option, T data);

    /**
     * 调用选项
     */
    class Option {

        private long timeoutMs = 30000L;

        /**
         * 获取调用超时时间（毫秒）
         *
         * @return 调用超时时间（毫秒）
         */
        public long timeoutMs() {
            return this.timeoutMs;
        }

        /**
         * 设置调用超时时间（毫秒）
         *
         * @param timeoutMs 调用超时时间（毫秒）
         * @return this
         */
        public Option timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

    }

}
