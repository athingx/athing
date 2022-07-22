module athing.platform {

    exports io.github.athingx.athing.platform.builder;
    exports io.github.athingx.athing.platform.builder.client;
    exports io.github.athingx.athing.platform.builder.message;
    exports io.github.athingx.athing.platform.message;

    requires transitive athing.platform.api;

    requires jakarta.jms.api;
    requires org.slf4j;
    requires aliyun.java.sdk.core.v5;
    requires aliyun.java.sdk.iot.v5;
    requires qpid.jms.client;
    requires java.naming;


}