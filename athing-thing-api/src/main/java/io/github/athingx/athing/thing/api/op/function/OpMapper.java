package io.github.athingx.athing.thing.api.op.function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.op.OpReply;
import io.github.athingx.athing.thing.api.op.OpRequest;

import java.nio.charset.Charset;

import static java.util.Optional.ofNullable;

/**
 * 操作映射函数
 *
 * @param <T> 映射前类型
 * @param <R> 映射后类型
 */
@FunctionalInterface
public interface OpMapper<T, R> extends OpFunction<T, R> {

    /**
     * 将字节数组映射为json
     *
     * @param charset 字符编码
     * @return {@code bytes->json}
     */
    static OpMapper<byte[], String> mappingBytesToJson(Charset charset) {
        return (topic, data) -> new String(data, charset);
    }

    /**
     * 将json映射为指定类型
     *
     * @param type 指定类型
     * @param <R>  指定类型
     * @return {@code json->type}
     */
    static <R> OpMapper<String, R> mappingJsonToType(Class<R> type) {
        return (topic, json) -> GsonFactory.getGson().fromJson(json, type);
    }

    /**
     * 将json映射为指定类型
     *
     * @param tToken 指定类型
     * @param <R>    指定类型
     * @return {@code json->type}
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
        return (topic, json) -> {
            final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return new OpReply<>(
                    ofNullable(root.get("id"))
                            .map(JsonElement::getAsString)
                            .orElseThrow(() -> new IllegalArgumentException("missing id")),
                    ofNullable(root.get("code"))
                            .map(JsonElement::getAsInt)
                            .orElseThrow(() -> new IllegalArgumentException("missing code")),
                    ofNullable(root.get("message"))
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    ofNullable(root.get("data"))
                            .filter(element -> !element.getAsJsonObject().keySet().isEmpty())
                            .map(element -> GsonFactory.getGson().fromJson(element, type))
                            .orElse(null)
            );
        };
    }

    static <T> OpMapper<String, OpRequest<T>> mappingJsonToOpRequest(Class<T> type) {
        return (topic, json) -> {
            final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return new OpRequest<>(
                    ofNullable(root.get("id"))
                            .map(JsonElement::getAsString)
                            .orElseThrow(() -> new IllegalArgumentException("missing id")),
                    ofNullable(root.get("version"))
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    ofNullable(root.get("method"))
                            .map(JsonElement::getAsString)
                            .orElseThrow(() -> new IllegalArgumentException("missing method")),
                    ofNullable(root.get("sys"))
                            .filter(element -> !element.getAsJsonObject().keySet().isEmpty())
                            .map(element -> GsonFactory.getGson().fromJson(element, OpRequest.Ext.class))
                            .orElse(null),
                    ofNullable(root.get("params"))
                            .filter(element -> !element.getAsJsonObject().keySet().isEmpty())
                            .map(element -> GsonFactory.getGson().fromJson(element, type))
                            .orElse(null)
            );
        };
    }

}
