package io.github.athingx.athing.thing;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.client.DefaultMqttClientFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class LoadingThing implements LoadingProperties {

    static volatile Thing thing;

    @BeforeClass
    public static void onBeforeClass() throws Exception {
        thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .client(new DefaultMqttClientFactory()
                        .remote(REMOTE)
                        .secret(SECRET)
                )
                .build();
    }

    @AfterClass
    public static void onAfterClass() {
        thing.destroy();
    }

}
