package io.github.athingx.athing.thing.api.op.function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.op.domain.OpRequest;
import io.github.athingx.athing.thing.api.op.domain.OpResponse;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import static java.util.Optional.ofNullable;

/**
 * 操作数据解码器
 *
 * @param <T> 解码前数据类型
 * @param <R> 解码后数据类型
 */
@FunctionalInterface
public interface OpDecoder<T, R> {

    /**
     * 解码
     *
     * @param topic 主题
     * @param data  解码前数据
     * @return 解码后数据
     */
    R decode(String topic, T data);

    /**
     * 解码器后置拼接
     * <p>
     * 拼接后的解码器先执行当前解码器，再执行after解码器。如果当前解码器解码后数据为null，则不执行after解码器
     *
     * @param after 后置解码器
     * @param <V>   后置解码器解码后数据类型
     * @return 拼接后的解码器
     */
    default <V> OpDecoder<T, V> then(OpDecoder<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (topic, data) -> Optional.ofNullable(decode(topic, data))
                .map(ret -> after.decode(topic, ret))
                .orElse(null);
    }

    /**
     * 解码前后返回相同的数据
     *
     * @param <V> 解码数据类型
     * @return Identity解码器
     */
    static <V> OpDecoder<V, V> identity() {
        return (topic, data) -> data;
    }

    /**
     * 对于符合过滤条件的返回解码前数据，否则返回null
     *
     * @param filter 过滤函数
     * @param <V>    解码数据类型
     * @return 过滤解码器
     */
    static <V> OpDecoder<V, V> filter(BiPredicate<String, V> filter) {
        Objects.requireNonNull(filter);
        return (topic, data) -> filter.test(topic, data) ? data : null;
    }

    /**
     * 将字节数组解码为Json字符串
     *
     * @param charset 字符串编码
     * @return {@code byte[]->Json}
     */
    static OpDecoder<byte[], String> decodeByteToJson(Charset charset) {
        Objects.requireNonNull(charset);
        return (topic, data) -> new String(data, charset);
    }

    /**
     * 将Json字符串解码为指定类型
     *
     * @param type 指定类型
     * @param <V>  解码数据类型
     * @return {@code Json->V}
     */
    static <V> OpDecoder<String, V> decodeJsonToType(Class<V> type) {
        Objects.requireNonNull(type);
        return (topic, data) -> GsonFactory.getGson().fromJson(data, type);
    }

    /**
     * 将Json字符串解码为指定类型
     *
     * @param tToken 指定类型（支持泛型）
     * @param <V>    解码数据类型
     * @return {@code Json->V}
     */
    static <V> OpDecoder<String, V> decodeJsonToType(TypeToken<V> tToken) {
        Objects.requireNonNull(tToken);
        return (topic, data) -> GsonFactory.getGson().fromJson(data, tToken.getType());
    }

    /**
     * 将Json字符串解码为{@link OpResponse}
     *
     * @param type 指定{@link OpResponse}的数据类型
     * @param <V>  {@link OpResponse}的数据类型
     * @return {@code Json->OpResponse<V>}
     */
    static <V> OpDecoder<String, OpResponse<V>> decodeJsonToOpResponse(Class<V> type) {
        return (topic, json) -> {
            final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return OpResponse.of(
                    ofNullable(root.get("id"))
                            .map(JsonElement::getAsString)
                            .orElseThrow(() -> new IllegalArgumentException("token is required")),
                    ofNullable(root.get("code"))
                            .map(JsonElement::getAsInt)
                            .orElseThrow(() -> new IllegalArgumentException("code is required")),
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
     * 将Json字符串解码为{@link OpRequest}
     *
     * @param type 指定{@link OpRequest}的数据类型
     * @param <V>  {@link OpRequest}的数据类型
     * @return {@code Json->OpRequest<V>}
     */
    static <V> OpDecoder<String, OpRequest<V>> decodeJsonToOpRequest(Class<V> type) {
        return (topic, json) -> {
            final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return new OpRequest<>(
                    ofNullable(root.get("id"))
                            .map(JsonElement::getAsString)
                            .orElseThrow(() -> new IllegalArgumentException("token is required")),
                    ofNullable(root.get("version"))
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    ofNullable(root.get("method"))
                            .map(JsonElement::getAsString)
                            .orElse(null),
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
