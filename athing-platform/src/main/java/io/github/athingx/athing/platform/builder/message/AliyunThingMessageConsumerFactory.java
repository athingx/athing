package io.github.athingx.athing.platform.builder.message;

import io.github.athingx.athing.platform.api.message.ThingMessageListener;
import io.github.athingx.athing.platform.impl.message.ThingMessageConsumerImpl;
import io.github.athingx.athing.platform.message.ThingMessageConsumer;
import jakarta.jms.JMSException;
import jakarta.jms.Session;

import static io.github.athingx.athing.platform.impl.util.IOUtils.closeQuietly;
import static java.util.Objects.requireNonNull;

/**
 * 阿里云设备消息消费者工厂
 */
public class AliyunThingMessageConsumerFactory implements ThingMessageConsumerFactory {

    private String queue;
    private JmsConnectionFactory connectionFactory = new AliyunJmsConnectionFactory();
    private ThingMessageListener listener;

    /**
     * 消息队列名
     *
     * @param queue 消息队列名
     * @return this
     */
    public AliyunThingMessageConsumerFactory queue(String queue) {
        this.queue = queue;
        return this;
    }

    /**
     * JMS连接工厂
     *
     * @param jmsConnectionFactory JMS连接工厂
     * @return this
     */
    public AliyunThingMessageConsumerFactory connection(JmsConnectionFactory jmsConnectionFactory) {
        this.connectionFactory = jmsConnectionFactory;
        return this;
    }

    /**
     * 设备消息监听器
     *
     * @param listener 设备消息监听器
     * @return this
     */
    public AliyunThingMessageConsumerFactory listener(ThingMessageListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public ThingMessageConsumer make() throws JMSException {
        requireNonNull(queue, "queue is required!");
        requireNonNull(listener, "listener is required!");
        requireNonNull(connectionFactory, "connection is required!");
        final var connection = connectionFactory.make();
        try {
            final var session = connection.createSession(Session.CLIENT_ACKNOWLEDGE);
            final var consumer = session.createConsumer(session.createQueue(queue));
            final var name = "thing-message-consumer://%s".formatted(queue);
            return new ThingMessageConsumerImpl(name, consumer, listener) {

                @Override
                public void close() throws Exception {
                    connection.close();
                    super.close();
                }

            };
        } catch (Throwable cause) {
            closeQuietly(connection);
            if (cause instanceof JMSException jmsCause) {
                throw jmsCause;
            } else {
                throw new RuntimeException("init jms error!", cause);
            }
        }

    }

}
