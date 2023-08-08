package io.github.athingx.athing.thing;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.client.DefaultMqttClientFactory;
import io.github.athingx.athing.thing.builder.client.MqttConnectStrategy;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * 设备连接测试用例
 */
public class ThingConnectTestCase implements LoadingProperties {

    @Test
    public void test$thing$connect$success() throws Exception {
        final Thing thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .client(new DefaultMqttClientFactory(REMOTE, SECRET))
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
                .client(new DefaultMqttClientFactory(REMOTE, SECRET)
                        .strategy((path, client, options, isReconnect) ->
                                new Thread(() -> {
                                    try {
                                        latch.await();
                                        MqttConnectStrategy
                                                .always(30 * 1000L, 3 * 60 * 1000L)
                                                .connect(path, client, options, isReconnect);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }).start())
                )
                .build();
        Assert.assertNotNull(thing);

        var future = thing.op().bind("/hello")
                .consume((topic, bytes) -> {

                });
        latch.countDown();
        future.get().unbind().get();
        thing.destroy();
    }

    @Test(expected = MqttException.class)
    public void test$thing$connect$limits_retry() throws Exception {
        final Thing thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .client(new DefaultMqttClientFactory("tcp://imposable.com:0", SECRET)
                        .connOpt(options -> {
                            options.setConnectionTimeout(10);
                            options.setMaxReconnectDelay(10);
                            return options;
                        })
                        .strategy(MqttConnectStrategy.retry(3, 500, 2000))
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
