package io.github.athingx.athing.platform;

import io.github.athingx.athing.platform.builder.ThingPlatformBuilder;
import io.github.athingx.athing.platform.builder.client.AliyunIAcsClientFactory;
import io.github.athingx.athing.platform.builder.message.AliyunJmsConnectionFactory;
import io.github.athingx.athing.platform.builder.message.AliyunThingMessageConsumerFactory;
import io.github.athingx.athing.platform.mock.MockThingTemplate;
import org.junit.Test;

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

    @Test(expected = IllegalArgumentException.class)
    public void platform$mock$exception() throws Exception {

        final var platform = new ThingPlatformBuilder()
                .client(new AliyunIAcsClientFactory()
                        .region("cn-shanghai")
                        .identity(PLATFORM_IDENTITY)
                        .secret(PLATFORM_SECRET))
                .build();

        platform.register(MockThingTemplate.class, (client, productId, thingId) -> null);
        platform.register(MockThingTemplate.class, (client, productId, thingId) -> null);

    }

}
