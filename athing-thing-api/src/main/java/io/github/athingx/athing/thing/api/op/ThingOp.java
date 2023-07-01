package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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
     * @param opPost 投递操作
     * @param opData 投递数据
     * @param <V>    数据类型
     * @return 投递结果
     */
    <V extends OpData>
    CompletableFuture<Void> post(OpPost<? super V> opPost, V opData);

    /**
     * 绑定设备数据消费
     *
     * @param opBind    绑定操作
     * @param consumeFn 消费函数
     * @param <V>       消费数据类型
     * @return 消费操作
     */
    <V> CompletableFuture<ThingBind> bind(OpBind<? extends V> opBind, BiConsumer<String, ? super V> consumeFn);

    /**
     * 绑定设备数据调用
     *
     * @param opPost 请求操作
     * @param opBind 应答操作
     * @param <T>    请求数据类型
     * @param <R>    应答数据类型
     * @return 调用操作
     */
    <T extends OpData, R extends OpData>
    CompletableFuture<? extends ThingCall<? super T, ? extends R>> bind(
            OpPost<? super T> opPost,
            OpBind<? extends R> opBind
    );

}
