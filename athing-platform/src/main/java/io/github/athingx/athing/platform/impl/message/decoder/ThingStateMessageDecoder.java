package io.github.athingx.athing.platform.impl.message.decoder;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.platform.api.message.ThingStateMessage;
import io.github.athingx.athing.platform.api.message.decoder.DecodeException;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 设备状态消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-2ll-4j3-1wx">设备上下线状态</a>
 */
public class ThingStateMessageDecoder implements ThingMessageDecoder<ThingStateMessage> {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public ThingStateMessage[] decode(String jmsMessageId, String jmsMessageTopic, String jmsMessageBody) throws DecodeException {

        if (!jmsMessageTopic.matches("/as/mqtt/status/[^/]+/[^/]+")) {
            return null;
        }

        final Data data = gson.fromJson(jmsMessageBody, Data.class);
        final SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            final long utcOccurTimestamp = utcDateFormat.parse(data.utcTime).getTime();
            final long utcLastTimestamp = utcDateFormat.parse(data.utcLastTime).getTime();
            return new ThingStateMessage[]{
                    new ThingStateMessage(
                            data.productId,
                            data.thingId,
                            utcOccurTimestamp,
                            ThingStateMessage.State.valueOf(data.status.toUpperCase()),
                            utcLastTimestamp,
                            data.clientIp
                    )
            };
        } catch (ParseException cause) {
            throw new DecodeException(
                    this,
                    "illegal utc format, occur=%s;last=%s;".formatted(data.utcTime, data.utcLastTime),
                    cause
            );
        }
    }


    /**
     * 数据
     */
    private static class Data {

        @SerializedName("status")
        String status;

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("time")
        String time;

        @SerializedName("utcTime")
        String utcTime;

        @SerializedName("lastTime")
        String lastTime;

        @SerializedName("utcLastTime")
        String utcLastTime;

        @SerializedName("clientIp")
        String clientIp;

    }

}
