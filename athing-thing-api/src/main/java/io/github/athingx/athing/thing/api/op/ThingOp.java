package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.ThingPath;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 设备操作
 *
 * @param <T> 操作请求数据类型
 * @param <R> 操作应答数据类型
 */
public interface ThingOp<T, R> {

    /**
     * 设备路径
     *
     * @return 设备路径
     */
    ThingPath path();

    /**
     * 生成一个令牌
     *
     * @return 令牌
     */
    String genToken();

    /**
     * 投递数据
     *
     * @param topic 主题
     * @param data  数据
     * @return 投递结果
     */
    CompletableFuture<Void> post(String topic, T data);

    /**
     * 绑定消费数据操作
     *
     * @param express  订阅表达式
     * @param consumer 消费函数
     * @return 绑定结果
     */
    CompletableFuture<OpConsumer> consumer(String express, BiConsumer<String, R> consumer);

    /**
     * 绑定调用操作
     *
     * @param express 订阅表达式
     * @param codec   编解码器
     * @param <UT>    调用请求类型
     * @param <UR>    调用应答类型
     * @return 绑定结果
     */
    <UT extends OpData, UR extends OpData>
    CompletableFuture<OpTopicCaller<UT, UR>> caller(String express, Codec<T, R, UT, UR> codec);

    /**
     * 设备操作请求数据编码
     *
     * @param encoder 编码器
     * @param <U>     编码后的数据类型
     * @return 设备操作
     */
    default <U> ThingOp<U, R> encode(Encoder<U, T> encoder) {
        return codec(new Codec<>() {
            @Override
            public T encode(U u) {
                return encoder.encode(u);
            }

            @Override
            public R decode(String topic, R r) {
                return r;
            }
        });
    }

    /**
     * 设备操作应答数据解码
     *
     * @param decoder 解码器
     * @param <U>     解码后的数据类型
     * @return 设备操作
     */
    default <U> ThingOp<T, U> decode(Decoder<R, U> decoder) {
        return codec(new Codec<>() {
            @Override
            public T encode(T t) {
                return t;
            }

            @Override
            public U decode(String topic, R r) {
                return decoder.decode(topic, r);
            }
        });
    }

    /**
     * 设备操作编解码
     *
     * @param codec 编解码器
     * @param <UT>  操作请求类型
     * @param <UR>  操作应答类型
     * @return 设备操作
     */
    <UT, UR> ThingOp<UT, UR> codec(Codec<T, R, UT, UR> codec);

}
