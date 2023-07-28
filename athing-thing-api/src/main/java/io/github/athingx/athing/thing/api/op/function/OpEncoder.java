package io.github.athingx.athing.thing.api.op.function;

import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.op.domain.OpRequest;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 操作编码器
 *
 * @param <T> 编码前数据类型
 * @param <R> 编码后数据类型
 */
@FunctionalInterface
public interface OpEncoder<T, R> {

    /**
     * 编码
     *
     * @param token 操作令牌
     * @param data  编码前数据
     * @return 编码后数据
     */
    R encode(String token, T data);

    /**
     * 编码器前置拼接
     *
     * @param before 前置编码器
     * @param <V>    前置编码器编码前数据类型
     * @return 拼接后的编码器
     */
    default <V> OpEncoder<V, R> compose(OpEncoder<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (token, v) -> encode(token, before.encode(token, v));
    }

    /**
     * 编码前后返回相同的数据
     *
     * @param <V> 编码数据类型
     * @return Identity编码器
     */
    static <V> OpEncoder<V, V> identity() {
        return (token, data) -> data;
    }

    /**
     * 编码字符串为字节数组
     *
     * @param charset 字符集
     * @return {@code byte[]<-Json}
     */
    static OpEncoder<String, byte[]> encodeByteFromJson(Charset charset) {
        return (token, data) -> data.getBytes(charset);
    }

    /**
     * 编码指定类型为Json字符串
     *
     * @param type 指定类型
     * @param <V>  编码数据类型
     * @return {@code Json<-V}
     */
    static <V> OpEncoder<V, String> encodeJsonFromType(Class<V> type) {
        return (token, data) -> GsonFactory.getGson().toJson(data, type);
    }

    /**
     * 编码指定类型为Json字符串
     *
     * @param tToken 指定类型（支持泛型）
     * @param <V>    编码数据类型
     * @return {@code Json<-V}
     */
    static <V> OpEncoder<V, String> encodeJsonFromType(TypeToken<V> tToken) {
        return (token, data) -> GsonFactory.getGson().toJson(data, tToken.getType());
    }

    /**
     * 编码{@link OpRequest}为Json字符串
     *
     * @param type 指定{@link OpRequest}的数据类型
     * @param <V> {@link OpRequest}的数据类型
     * @return {@code Json<-OpRequest<V>}
     */
    static <V> OpEncoder<OpRequest<V>, String> encodeJsonFromOpRequest(Class<V> type) {
        return encodeJsonFromType(new TypeToken<>() {
        });
    }

}
