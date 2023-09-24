package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.common.gson.GsonFactory;

import java.nio.charset.Charset;

/**
 * 编码器
 *
 * @param <U> 源类型
 * @param <T> 目标类型
 */
public interface Encoder<U, T> {

    /**
     * 编码
     *
     * @param u 源数据
     * @return 目标数据
     */
    T encode(U u);

    /**
     * 链接编码器
     *
     * @param before 编码器
     * @param <V>    源类型
     * @return 链接后的编码器
     */
    default <V> Encoder<V, T> compose(Encoder<? super V, ? extends U> before) {
        return v -> encode(before.encode(v));
    }

    /**
     * 将json编码为字节数组
     *
     * @param charset 字符编码
     * @return {@code json->bytes}
     */
    static Encoder<String, byte[]> encodeJsonToBytes(Charset charset) {
        return json -> json.getBytes(charset);
    }

    /**
     * 将对象编码为json
     *
     * @param type 指定类型
     * @param <T>  指定类型
     * @return {@code type->json}
     */
    static <T> Encoder<T, String> encodeTypeToJson(Class<T> type) {
        return object -> GsonFactory.getGson().toJson(object, type);
    }

}
