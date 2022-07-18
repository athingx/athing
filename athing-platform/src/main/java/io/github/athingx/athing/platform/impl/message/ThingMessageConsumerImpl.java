package io.github.athingx.athing.platform.impl.message;

import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import io.github.athingx.athing.platform.api.message.decoder.ThingLifeCycleMessageDecoder;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;
import io.github.athingx.athing.platform.api.message.decoder.ThingStateMessageDecoder;
import io.github.athingx.athing.platform.message.ThingMessageConsumer;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableCollection;

/**
 * 设备消息消费者
 */
public class ThingMessageConsumerImpl implements ThingMessageConsumer {

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
    public ThingMessageConsumerImpl(String name, MessageConsumer consumer, ThingMessageListener listener) throws JMSException {
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

    @Override
    public void close() throws Exception {
        consumer.close();
        logger.debug("{} closed!", this);
    }

    @Override
    public void decoders(Consumer<Set<ThingMessageDecoder<?>>> setupFn) {
        setupFn.accept(decoders);
    }

    @Override
    public Collection<ThingMessageDecoder<?>> decoders() {
        return unmodifiableCollection(decoders);
    }

}
