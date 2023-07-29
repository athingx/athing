package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.function.OpSupplier;

import java.util.concurrent.CompletableFuture;

/**
 * 设备调用操作
 *
 * @param <T> 请求数据类型
 * @param <R> 应答数据类型
 */
public interface ThingOpCaller<T, R> extends ThingOpBinder {

    /**
     * 数据调用
     *
     * @param supplier 获取请求函数
     * @return 应答结果
     */
    CompletableFuture<R> call(OpSupplier<T> supplier);

    /**
     * 数据调用
     *
     * @param option   调用选项
     * @param supplier 获取请求函数
     * @return 应答结果
     */
    CompletableFuture<R> call(Option option, OpSupplier<T> supplier);

    /**
     * 数据调用
     *
     * @param request 请求
     * @return 应答结果
     */
    default CompletableFuture<R> call(T request) {
        return call(token -> request);
    }

    /**
     * 数据调用
     *
     * @param option  调用选项
     * @param request 请求
     * @return 应答结果
     */
    default CompletableFuture<R> call(Option option, T request) {
        return call(option, token -> request);
    }

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
