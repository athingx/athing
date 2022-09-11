package io.github.athingx.athing.thing;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.mqtt.AliyunMqttClientFactory;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static io.github.athingx.athing.thing.builder.mqtt.AliyunMqttClientFactory.ConnectStrategy.limitsReTry;

/**
 * 设备连接测试用例
 */
public class ThingConnectTestCase implements LoadingProperties {

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
        Assert.assertEquals(PRODUCT_ID, thing.path().getProductId());
        Assert.assertEquals(THING_ID, thing.path().getThingId());
        Assert.assertEquals("%s/%s".formatted(PRODUCT_ID, THING_ID), thing.path().toURN());
        Assert.assertEquals("thing://%s/%s".formatted(PRODUCT_ID, THING_ID), thing.path().toURI().toString());
        Assert.assertEquals("thing://%s/%s".formatted(PRODUCT_ID, THING_ID), thing.path().toString());
        Assert.assertNotNull(thing.executor());
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
                        .strategy((path, options, client) -> {
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
        var future = thing.op().binding("/hello").bind((s, bytes) -> {

        });
        latch.countDown();
        future.get().unbind().get();
        thing.destroy();
    }

    @Test(expected = MqttException.class)
    public void test$thing$connect$limits_retry() throws Exception {
        final Thing thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote("tcp://imposable.com:0")
                        .strategy(limitsReTry(3, 1000))
                )
                .build();
        Assert.assertNotNull(thing);
        Assert.assertEquals(PRODUCT_ID, thing.path().getProductId());
        Assert.assertEquals(THING_ID, thing.path().getThingId());
        Assert.assertEquals("%s/%s".formatted(PRODUCT_ID, THING_ID), thing.path().toURN());
        Assert.assertEquals("thing://%s/%s".formatted(PRODUCT_ID, THING_ID), thing.path().toURI().toString());
        Assert.assertEquals("thing://%s/%s".formatted(PRODUCT_ID, THING_ID), thing.path().toString());
        thing.destroy();
    }

}
