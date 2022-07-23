package io.github.athingx.athing.thing.api.function;

import io.github.athingx.athing.common.GsonFactory;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Json映射函数
 *
 * @param <T> 原始类型
 * @param <R> 目标类型
 */
@FunctionalInterface
public interface ThingFnMapJson<T, R> extends ThingFnMap<T, R> {

    /**
     * 字节数组映射成Json：
     * {@code byte -> json}
     *
     * @param charset 字符编码
     * @return 映射函数
     */
    static ThingFnMapJson<byte[], String> mappingJsonFromBytes(Charset charset) {
        return (topic, bytes) -> new String(bytes, charset);
    }

    /**
     * Json映射成对象：
     * {@code json -> object}
     *
     * @param type 对象类型
     * @param <V>  对象类型
     * @return 映射函数
     */
    static <V> ThingFnMapJson<String, V> mappingJsonToType(Class<V> type) {
        return (topic, json) -> GsonFactory.getGson().fromJson(json, type);
    }

}
