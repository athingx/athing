package io.github.athingx.athing.platform.api.message.decoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.platform.api.message.ThingReplyMessage;

import java.util.Objects;

/**
 * 设备应答消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-9p8-2jl-sv4">设备下行指令结果</a>
 */
public abstract class ThingReplyMessageDecoder implements ThingMessageDecoder<ThingReplyMessage> {

    @Override
    public ThingReplyMessage[] decode(String jmsMessageId, String jmsMessageTopic, String jmsMessageBody) throws DecodeException {

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
    abstract protected ThingReplyMessage[] decode(ReplyHeader header, JsonObject root) throws DecodeException;

    /**
     * 应答头
     */
    protected static class ReplyHeader {

        @SerializedName("productKey")
        public final String productId;

        @SerializedName("deviceName")
        public final String thingId;

        @SerializedName("topic")
        public final String topic;

        @SerializedName("gmtCreate")
        public final long timestamp;

        @SerializedName("requestId")
        public final String token;

        @SerializedName("code")
        public final int code;

        @SerializedName("message")
        public final String message;

        private ReplyHeader(String productId, String thingId, String topic, long timestamp, String token, int code, String message) {
            this.productId = productId;
            this.thingId = thingId;
            this.topic = topic;
            this.timestamp = timestamp;
            this.token = token;
            this.code = code;
            this.message = message;
        }

    }

}
