module athing.thing {

    exports io.github.athingx.athing.thing.builder;
    exports io.github.athingx.athing.thing.builder.mqtt;
    exports io.github.athingx.athing.thing.builder.executor;

    requires transitive athing.thing.api;
    requires org.slf4j;
    requires org.eclipse.paho.client.mqttv3;

}