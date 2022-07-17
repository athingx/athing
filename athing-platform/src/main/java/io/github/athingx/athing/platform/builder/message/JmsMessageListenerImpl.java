package io.github.athingx.athing.platform.builder.message;

import io.github.athingx.athing.platform.api.message.ThingMessage;
import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import io.github.athingx.athing.platform.api.message.decoder.DecodeException;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * JMS消息监听器实现
 */
class JmsMessageListenerImpl implements MessageListener {

    private final Set<ThingMessageDecoder<?>> decoders;
    private final ThingMessageListener listener;

    JmsMessageListenerImpl(final Set<ThingMessageDecoder<?>> decoders,
                           final ThingMessageListener listener) {
        this.decoders = decoders;
        this.listener = listener;
    }

    private ThingMessage[] decode(String jmsMessageId, String jmsMessageTopic, String jmsMessageBody) throws DecodeException {

        // 尝试进行解码
        for (final ThingMessageDecoder<?> decoder : decoders) {
            try {
                final ThingMessage[] messages = decoder.decode(jmsMessageId, jmsMessageTopic, jmsMessageBody);
                if (null != messages && messages.length > 0) {
                    return messages;
                }
            } catch (Exception cause) {
                throw new DecodeException(decoder, cause);
            }
        }

        // 解码失败
        throw new DecodeException("none decoded!");

    }

    private String parseMessageId(Message jmsMessage) {
        try {
            return jmsMessage.getStringProperty("messageId");
        } catch (JMSException e) {
            throw new RuntimeException("parse jms-message messageId error!");
        }
    }

    private String parseTopic(Message jmsMessage, String messageId) {
        try {
            return jmsMessage.getStringProperty("topic");
        } catch (JMSException e) {
            throw new RuntimeException("parse jms-message: %s topic error!".formatted(messageId));
        }
    }

    private byte[] parseBody(Message jmsMessage, String messageId) {
        try {
            return jmsMessage.getBody(byte[].class);
        } catch (JMSException e) {
            throw new RuntimeException("parse jms-message: %s body error!".formatted(messageId));
        }
    }

    @Override
    public void onMessage(Message jmsMessage) {

        final String jmsMessageId = parseMessageId(jmsMessage);
        final String jmsMessageTopic = parseTopic(jmsMessage, jmsMessageId);
        final String jmsMessageBody = new String(parseBody(jmsMessage, jmsMessageId), UTF_8);

        // JMS消息解码
        final ThingMessage[] messages;
        try {
            messages = decode(jmsMessageId, jmsMessageTopic, jmsMessageBody);
        } catch (DecodeException cause) {
            throw new RuntimeException(
                    "decode jms-message error! message-id=%s;topic=%s;".formatted(jmsMessageId, jmsMessageTopic),
                    cause
            );
        }

        // 处理设备消息
        for (final ThingMessage message : messages) {
            try {
                listener.onMessage(message);
            } catch (Exception cause) {
                throw new RuntimeException(
                        "handle thing-message error! message-id=%s;topic=%s;thing-message=%s;".formatted(jmsMessageId, jmsMessageTopic, message),
                        cause
                );
            }
        }

    }

}
