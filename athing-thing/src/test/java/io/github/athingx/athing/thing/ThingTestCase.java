package io.github.athingx.athing.thing;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.aliyun.AliyunMqttClientFactory;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class ThingTestCase implements LoadingProperties {

    @Test
    public void test$thing$connect$success() throws Exception {
        final Thing thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();
        Assert.assertNotNull(thing);
        thing.destroy();
    }

    @Test
    public void test$thing$connect_after_bind$success() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final Thing thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                        .connecting((path, options, client) -> {
                            client.setBufferOpts(new DisconnectedBufferOptions() {{
                                setBufferEnabled(true);
                                setPersistBuffer(false);
                            }});
                            new Thread(() -> {
                                try {
                                    latch.await();
                                    AliyunMqttClientFactory.ConnectStrategy
                                            .alwaysReTry(options.getMaxReconnectDelay())
                                            .connect(path, options, client);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }).start();

                        })
                )
                .build();
        Assert.assertNotNull(thing);
        var future = thing.op().bind("/hello").bind((s, bytes) -> {

        });

        latch.countDown();
        future.get().unbind();
        thing.destroy();

    }

}
