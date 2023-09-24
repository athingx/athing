package io.github.athingx.athing.thing.api.op;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.gson.GsonFactory;

import java.nio.charset.Charset;

import static java.util.Optional.ofNullable;

/**
 * 解码器
 *
 * @param <T> 源类型
 * @param <U> 目标类型
 */
public interface Decoder<T, U> {

    /**
     * 解码
     *
     * @param topic 主题
     * @param t     源数据
     * @return 目标数据
     */
    U decode(String topic, T t);

    /**
     * 链接解码器
     *
     * @param decoder 解码器
     * @param <V>     目标类型
     * @return 链接后的解码器
     */
    default <V> Decoder<T, V> then(Decoder<? super U, ? extends V> decoder) {
        return (topic, t) -> decoder.decode(topic, decode(topic, t));
    }

    /**
     * 将字节数组解码为json
     *
     * @param charset 字符编码
     * @return {@code bytes->json}
     */
    static Decoder<byte[], String> decodeBytesToJson(Charset charset) {
        return (topic, data) -> new String(data, charset);
    }

    /**
     * 将json解码为指定类型
     *
     * @param type 指定类型
     * @param <R>  指定类型
     * @return {@code json->type}
     */
    static <R> Decoder<String, R> decodeJsonToType(Class<R> type) {
        return (topic, json) -> GsonFactory.getGson().fromJson(json, type);
    }

    /**
     * 将json解码为指定类型
     *
     * @param tToken 指定类型
     * @param <R>    指定类型
     * @return {@code json->type}
     */
    static <R> Decoder<String, R> decodeJsonToType(TypeToken<R> tToken) {
        return (topic, json) -> GsonFactory.getGson().fromJson(json, tToken.getType());
    }

    /**
     * 将json解码为{@link OpReply}
     *
     * @param type 数据类型
     * @param <T>  数据类型
     * @return {@code json->}{@link OpReply}
     */
    static <T> Decoder<String, OpReply<T>> decodeJsonToOpReply(Class<T> type) {
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

    /**
     * 将json解码为{@link OpRequest}
     *
     * @param type 数据类型
     * @param <T>  数据类型
     * @return {@code json->}{@link OpRequest}
     */
    static <T> Decoder<String, OpRequest<T>> decodeJsonToOpRequest(Class<T> type) {
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
