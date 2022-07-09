package io.github.athingx.athing.platform.api.message.decoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.platform.api.message.ThingMessage;

import java.util.Objects;

/**
 * 设备应答消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-9p8-2jl-sv4">设备下行指令结果</a>
 */
public abstract class ThingReplyMessageDecoder implements ThingMessageDecoder {

    @Override
    public ThingMessage[] decode(String jmsMessageId, String jmsMessageTopic, String jmsMessageBody) throws DecodeException {

        // 检查是否设备应答返回消息
        if (!jmsMessageTopic.matches("^/[^/]+/[^/]+/thing/downlink/reply/message$")) {
            return null;
        }

        // 解析应答头信息
        final JsonObject root = JsonParser.parseString(jmsMessageBody).getAsJsonObject();

        // 解析应答
        final ReplyHeader header = GsonFactory.getGson().fromJson(root, ReplyHeader.class);
        Objects.requireNonNull(header.productId);
        Objects.requireNonNull(header.thingId);
        Objects.requireNonNull(header.topic);
        Objects.requireNonNull(header.token);

        // 继续进一步解码
        return decode(header, root);
    }

    /**
     * 应答解码
     *
     * @param header 应答头
     * @param root   消息体（JSON根节点）
     * @return 设备消息
     * @throws DecodeException 解码失败
     */
    abstract protected ThingMessage[] decode(ReplyHeader header, JsonObject root) throws DecodeException;

    /**
     * 应答头
     */
    protected static class ReplyHeader {

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("topic")
        String topic;

        @SerializedName("gmtCreate")
        long timestamp;

        @SerializedName("requestId")
        String token;

        @SerializedName("code")
        int code;

        @SerializedName("message")
        String message;

    }

}
