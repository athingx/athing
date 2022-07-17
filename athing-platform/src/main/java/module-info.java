module athing.platform {

    exports io.github.athingx.athing.platform.builder;
    exports io.github.athingx.athing.platform.builder.client;
    exports io.github.athingx.athing.platform.builder.message;
    opens io.github.athingx.athing.platform.impl.message.decoder to com.google.gson;

    requires transitive athing.platform.api;
    requires transitive java.naming;

    requires jakarta.jms.api;
    requires qpid.jms.client;
    requires org.slf4j;
    requires iot20180120;
    requires tea.openapi;


}