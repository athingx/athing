package io.github.athingx.athing.platform.api.message.decoder;

import io.github.athingx.athing.platform.api.message.ThingMessage;

/**
 * 设备消息解码器
 */
public interface ThingMessageDecoder<T extends ThingMessage> {

    /**
     * 解码
     *
     * @param jmsMessageId    JMS消息ID
     * @param jmsMessageTopic JMS消息主题
     * @param jmsMessageBody  JMS消息内容（JSON）
     * @return 设备消息
     * @throws DecodeException 解码异常
     */
    T[] decode(String jmsMessageId, String jmsMessageTopic, String jmsMessageBody) throws DecodeException;

}
