module athing.platform {

    exports io.github.athingx.athing.platform.builder;
    exports io.github.athingx.athing.platform.builder.iot;
    exports io.github.athingx.athing.platform.builder.jms;

    requires transitive athing.platform.api;
    requires org.slf4j;
    requires jakarta.jms.api;
    requires qpid.jms.client;

    requires iot20180120;


}