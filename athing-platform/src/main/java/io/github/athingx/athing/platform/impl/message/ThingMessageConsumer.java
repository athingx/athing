package io.github.athingx.athing.platform.impl.message;

import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import io.github.athingx.athing.platform.api.message.decoder.ThingMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import java.util.LinkedHashSet;
import java.util.Set;

public class ThingMessageConsumer implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageConsumer jmsMessageConsumer;
    private final Set<ThingMessageDecoder<?>> decoders = new LinkedHashSet<>();
    private final String _string;

    public ThingMessageConsumer(MessageConsumer jmsMessageConsumer, ThingMessageListener listener) throws JMSException {
        this.jmsMessageConsumer = jmsMessageConsumer;
        this._string = "thing-message://consumer";
        jmsMessageConsumer.setMessageListener(new ThingJmsMessageListenerImpl(decoders, listener) {

            @Override
            public void onMessage(Message jmsMessage) {
                try {

                    // 消费消息
                    super.onMessage(jmsMessage);

                    // 消费成功则提交消息
                    jmsMessage.acknowledge();

                } catch (Exception cause) {
                    logger.warn("{}/rollback", _string, cause);
                    throw new RuntimeException(cause);
                }

            }

        });
    }

    public Set<ThingMessageDecoder<?>> decoders() {
        return decoders;
    }

    @Override
    public void close() throws JMSException {
        jmsMessageConsumer.close();
    }

}
