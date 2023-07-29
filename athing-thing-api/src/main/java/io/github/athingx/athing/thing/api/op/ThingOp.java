package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.op.domain.OpData;
import io.github.athingx.athing.thing.api.op.function.OpConsumer;
import io.github.athingx.athing.thing.api.op.function.OpFunction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 设备操作
 */
public interface ThingOp {

    /**
     * 数据投递
     *
     * @param pub 投递端口
     * @param <V> 投递数据类型
     * @return 设备数据发布者
     */
    <V> CompletableFuture<ThingOpPoster<V>> poster(PubPort<V> pub);

    /**
     * 数据消费
     *
     * @param sub       订阅端口
     * @param consumeFn 数据消费函数
     * @param <V>       消费数据类型
     * @return 数据消费操作绑定
     */
    <V> CompletableFuture<ThingOpBinder> consumer(SubPort<V> sub, OpConsumer<V> consumeFn);

    /**
     * 绑定数据服务
     *
     * @param sub          订阅端口
     * @param routingPubFn 发布端口路由函数
     * @param serviceFn    数据服务函数
     * @param <T>          请求数据类型
     * @param <R>          应答数据类型
     * @return 数据服务操作绑定
     */
    <T extends OpData, R> CompletableFuture<ThingOpBinder> services(
            SubPort<T> sub,
            OpFunction<T, PubPort<R>> routingPubFn,
            OpFunction<T, CompletableFuture<R>> serviceFn
    );

    /**
     * 绑定数据调用
     *
     * @param pub 发布端口
     * @param sub 订阅端口
     * @param <T> 请求数据类型
     * @param <R> 应答数据类型
     * @return 设备数据调用者
     */
    <T, R extends OpData> CompletableFuture<ThingOpCaller<T, R>> caller(
            PubPort<T> pub,
            SubPort<R> sub
    );

}
