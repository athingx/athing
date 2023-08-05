package io.github.athingx.athing.thing.api.op.function;

import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.op.OpReply;

import java.nio.charset.Charset;

/**
 * 操作映射函数
 * @param <T> 映射前类型
 * @param <R> 映射后类型
 */
public interface OpMapper<T, R> extends OpFunction<T, R> {

    /**
     * 将字节数组映射为json
     * @param charset 字符编码
     * @return {@code bytes->json}
     */
    static OpMapper<byte[], String> mappingBytesToJson(Charset charset) {
        return (topic, data) -> new String(data, charset);
    }

    /**
     * 将json映射为指定类型
     * @param type 指定类型
     * @return {@code json->type}
     * @param <R> 指定类型
     */
    static <R> OpMapper<String, R> mappingJsonToType(Class<R> type) {
        return (topic, json) -> GsonFactory.getGson().fromJson(json, type);
    }

    /**
     * 将json映射为指定类型
     * @param tToken 指定类型
     * @return {@code json->type}
     * @param <R> 指定类型
     */
    static <R> OpMapper<String, R> mappingJsonToType(TypeToken<R> tToken) {
        return (topic, json) -> GsonFactory.getGson().fromJson(json, tToken.getType());
    }

    /**
     * 将json映射为{@link OpReply}
     *
     * @param type 数据类型
     * @param <T>  数据类型
     * @return {@code json->}{@link OpReply}
     */
    static <T> OpMapper<String, OpReply<T>> mappingJsonToOpReply(Class<T> type) {
        return mappingJsonToType(new TypeToken<>() {
        });
    }

}
