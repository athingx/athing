module athing.platform {

    exports io.github.athingx.athing.platform.builder;

    requires transitive athing.platform.api;
    requires org.slf4j;
    requires java.naming;
    requires jakarta.jms.api;

    requires aliyun.java.sdk.iot;
    requires aliyun.java.sdk.core;

}