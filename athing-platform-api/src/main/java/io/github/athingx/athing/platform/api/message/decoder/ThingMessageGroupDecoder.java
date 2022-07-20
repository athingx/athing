package io.github.athingx.athing.platform.api.message.decoder;

import io.github.athingx.athing.platform.api.message.ThingMessage;

/**
 * 消息解码组
 */
public class ThingMessageGroupDecoder implements ThingMessageDecoder<ThingMessage> {

    private final ThingMessageDecoder<?>[] decoders;

    /**
     * 消息解码组
     *
     * @param decoders 解码器集合
     */
    public ThingMessageGroupDecoder(ThingMessageDecoder<?>[] decoders) {
        this.decoders = decoders;
    }

    @Override
    public ThingMessage[] decode(String jmsMessageId, String jmsMessageTopic, String jmsMessageBody) throws DecodeException {
        for (final ThingMessageDecoder<?> decoder : decoders) {
            final ThingMessage[] messages = decoder.decode(jmsMessageId, jmsMessageTopic, jmsMessageBody);
            if (null != messages && messages.length > 0) {
                return messages;
            }
        }
        return null;
    }

}
