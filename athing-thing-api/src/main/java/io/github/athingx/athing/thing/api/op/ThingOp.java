package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * 设备操作
 */
public interface ThingOp {

    /**
     * 生成操作令牌
     *
     * @return 操作令牌
     */
    String genToken();

    /**
     * 投递数据
     *
     * @param pub  发布端口
     * @param data 发布数据
     * @param <V>  数据类型
     * @return 投递操作
     */
    <V> CompletableFuture<Void> post(PubPort<? super V> pub, V data);

    /**
     * 绑定数据消费
     *
     * @param sub       订阅端口
     * @param consumeFn 数据消费函数
     * @param <V>       消费数据类型
     * @return 数据消费绑定操作
     */
    <V>
    CompletableFuture<ThingBind> bindConsumer(
            SubPort<? extends V> sub,
            BiConsumer<String/*TOPIC*/, ? super V> consumeFn
    );

    /**
     * 绑定数据服务
     *
     * @param sub       订阅端口
     * @param pub       发布端口
     * @param serviceFn 数据服务函数
     * @param <T>       请求数据类型
     * @param <R>       应答数据类型
     * @return 数据服务绑定操作
     */
    <T extends OpData, R>
    CompletableFuture<ThingBind> bindServices(
            SubPort<? extends T> sub,
            PubPort<? super R> pub,
            BiFunction<String/*TOPIC*/, ? super T, CompletableFuture<? extends R>> serviceFn
    );

    /**
     * 绑定数据调用
     *
     * @param pub 发布端口
     * @param sub 订阅端口
     * @param <T> 请求数据类型
     * @param <R> 应答数据类型
     * @return 数据调用绑定操作
     */
    <T, R extends OpData> CompletableFuture<? extends ThingCall<? super T, ? extends R>> bindCaller(
            PubPort<? super T> pub,
            SubPort<? extends R> sub
    );

}
