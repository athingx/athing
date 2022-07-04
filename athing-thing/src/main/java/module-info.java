module athing.thing {

    exports io.github.athingx.athing.thing;
    exports io.github.athingx.athing.thing.function;
    exports io.github.athingx.athing.thing.builder;
    exports io.github.athingx.athing.thing.op;

    opens io.github.athingx.athing.thing.op to com.google.gson;

    requires athing.common;
    requires org.slf4j;
    requires org.eclipse.paho.client.mqttv3;

}