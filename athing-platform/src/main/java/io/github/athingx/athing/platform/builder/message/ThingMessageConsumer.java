package io.github.athingx.athing.platform.builder.message;

import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;
import io.github.athingx.athing.platform.impl.message.decoder.ThingLifeCycleMessageDecoder;
import io.github.athingx.athing.platform.impl.message.decoder.ThingStateMessageDecoder;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 设备消息消费者
 */
public class ThingMessageConsumer implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String _string;
    private final MessageConsumer consumer;

    /**
     * 设备消息解码器
     */
    private final Set<ThingMessageDecoder<?>> decoders = new CopyOnWriteArraySet<>(new ArrayList<>() {{

        // 设备生命周期消息
        add(new ThingLifeCycleMessageDecoder());

        // 设备状态消息
        add(new ThingStateMessageDecoder());

    }});

    /**
     * 构建设备消息消费者
     *
     * @param name     名称
     * @param consumer JMS消息消费者
     * @param listener 设备消息监听器
     * @throws JMSException 构建失败
     */
    public ThingMessageConsumer(String name, MessageConsumer consumer, ThingMessageListener listener) throws JMSException {
        this._string = name;
        this.consumer = consumer;
        setupMessageListener(consumer, listener);
    }

    /**
     * 设置消息监听器
     *
     * @param consumer JMS消息消费者
     * @param listener 设备消息监听器
     * @throws JMSException 设置失败
     */
    private void setupMessageListener(MessageConsumer consumer, ThingMessageListener listener) throws JMSException {
        consumer.setMessageListener(new JmsMessageListenerImpl(decoders, listener) {

            @Override
            public void onMessage(Message jmsMessage) {

                final String jmsMessageId;
                try {
                    jmsMessageId = jmsMessage.getJMSMessageID();
                } catch (JMSException cause) {
                    throw new RuntimeException(cause);
                }

                try {

                    // 消费消息
                    super.onMessage(jmsMessage);

                    // 消费成功则提交消息
                    jmsMessage.acknowledge();
                    logger.debug("{}/jms/{}/commit", _string, jmsMessageId);

                } catch (Exception cause) {
                    logger.warn("{}/jms/{}/rollback", _string, jmsMessageId, cause);
                    throw new RuntimeException(cause);
                }

            }

        });
    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 追加设备消息解码器
     *
     * @param decoder 解码器
     */
    public void appendDecoder(ThingMessageDecoder<?> decoder) {
        decoders.add(decoder);
    }

    /**
     * 移除设备消息解码器
     *
     * @param decoder 解码器
     */
    public void removeDecoder(ThingMessageDecoder<?> decoder) {
        decoders.remove(decoder);
    }

    @Override
    public void close() throws JMSException {
        consumer.close();
        logger.debug("{} closed!", this);
    }

}
