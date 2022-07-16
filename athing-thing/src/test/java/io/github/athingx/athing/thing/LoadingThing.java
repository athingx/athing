package io.github.athingx.athing.thing;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.aliyun.AliyunMqttClientFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.Executors;

public abstract class LoadingThing implements LoadingProperties {

    static volatile Thing thing;

    @BeforeClass
    public static void onBeforeClass() throws Exception {
        thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();
    }

    @AfterClass
    public static void onAfterClass() {
        thing.destroy();
    }

}
