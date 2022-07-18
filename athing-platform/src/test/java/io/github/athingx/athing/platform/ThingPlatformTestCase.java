package io.github.athingx.athing.platform;

import io.github.athingx.athing.platform.api.message.ThingMessage;
import io.github.athingx.athing.platform.builder.ThingPlatformBuilder;
import io.github.athingx.athing.platform.builder.client.AliyunIAcsClientFactory;
import io.github.athingx.athing.platform.builder.message.AliyunJmsConnectionFactory;
import io.github.athingx.athing.platform.builder.message.AliyunThingMessageConsumerFactory;
import io.github.athingx.athing.platform.impl.message.ThingMessageConsumerImpl;
import io.github.athingx.athing.platform.mock.MockJmsMessage;
import io.github.athingx.athing.platform.mock.MockMessageConsumer;
import io.github.athingx.athing.platform.mock.MockThingMessage;
import io.github.athingx.athing.platform.mock.MockThingTemplate;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class ThingPlatformTestCase implements LoadingProperties {

    @Test
    public void platform$init$success() throws Exception {
        final var platform = new ThingPlatformBuilder()
                .client(new AliyunIAcsClientFactory()
                        .region("cn-shanghai")
                        .identity(PLATFORM_IDENTITY)
                        .secret(PLATFORM_SECRET)
                )
                .consumer(new AliyunThingMessageConsumerFactory()
                        .queue(PLATFORM_JMS_GROUP)
                        .connection(new AliyunJmsConnectionFactory()
                                .queue(PLATFORM_JMS_GROUP)
                                .remote(PLATFORM_REMOTE)
                                .identity(PLATFORM_IDENTITY)
                                .secret(PLATFORM_SECRET)
                        )
                        .listener(message -> {

                        })
                )
                .build();
        platform.close();
    }

    @Test
    public void platform$mock$success() throws Exception {
        final Queue<ThingMessage> queue = new LinkedList<>();
        final var consumer = new MockMessageConsumer();
        final var platform = new ThingPlatformBuilder()
                .client(new AliyunIAcsClientFactory()
                        .region("cn-shanghai")
                        .identity(PLATFORM_IDENTITY)
                        .secret(PLATFORM_SECRET))
                .consumer(() -> new ThingMessageConsumerImpl("test", consumer, queue::offer))
                .build();

        final var mockThingMessage = new MockThingMessage("", "", 0L);
        final var mockThingTemplate = new MockThingTemplate();

        platform.register(MockThingTemplate.class, (client, productId, thingId) -> mockThingTemplate);
        platform.register((jmsMessageId, jmsMessageTopic, jmsMessageBody) -> {
            if ("/mock".equals(jmsMessageTopic)) {
                return new ThingMessage[]{
                        mockThingMessage
                };
            }
            return null;
        });

        consumer.getMessageListener().onMessage(MockJmsMessage.message(
                UUID.randomUUID().toString(),
                "/mock",
                """
                        """
        ));

        Assert.assertEquals(mockThingMessage, queue.poll());
        Assert.assertEquals(mockThingTemplate, platform.genThingTemplate(MockThingTemplate.class, PRODUCT_ID, THING_ID));
        platform.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void platform$mock$exception() throws Exception {

        final var platform = new ThingPlatformBuilder()
                .client(new AliyunIAcsClientFactory()
                        .region("cn-shanghai")
                        .identity(PLATFORM_IDENTITY)
                        .secret(PLATFORM_SECRET))
                .consumer(() -> new ThingMessageConsumerImpl("test", new MockMessageConsumer(), message -> {

                }))
                .build();

        platform.register(MockThingTemplate.class, (client, productId, thingId) -> null);
        platform.register(MockThingTemplate.class, (client, productId, thingId) -> null);

    }

}
