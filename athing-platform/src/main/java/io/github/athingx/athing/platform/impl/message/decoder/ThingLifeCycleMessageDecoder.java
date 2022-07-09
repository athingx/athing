package io.github.athingx.athing.platform.impl.message.decoder;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.platform.api.message.ThingLifeCycleMessage;
import io.github.athingx.athing.platform.api.message.ThingMessage;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;

import java.util.Date;

/**
 * 设备生命周期消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-0im-t30-d4l">设备生命周期变更</a>
 */
public class ThingLifeCycleMessageDecoder implements ThingMessageDecoder {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public ThingMessage[] decode(String jmsMessageId, String jmsMessageTopic, String jmsMessageBody) {

        if (!jmsMessageTopic.matches("^/[^/]+/[^/]+/thing/lifecycle")) {
            return null;
        }

        final Data data = gson.fromJson(jmsMessageBody, Data.class);
        return new ThingMessage[]{
                new ThingLifeCycleMessage(
                        data.productId,
                        data.thingId,
                        data.timestamp.getTime(),
                        ThingLifeCycleMessage.LifeCycle.valueOf(data.action.toUpperCase())
                )
        };
    }

    /**
     * 数据
     */
    private static class Data {

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("messageCreateTime")
        Date timestamp;

        @SerializedName("action")
        String action;

    }

}
