module athing.thing {

    exports io.github.athingx.athing.thing.builder;

    requires transitive athing.thing.api;
    requires org.slf4j;
    requires org.eclipse.paho.client.mqttv3;

}