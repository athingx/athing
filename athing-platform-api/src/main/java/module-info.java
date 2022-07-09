open module athing.platform.api {

    exports io.github.athingx.athing.platform.api;
    exports io.github.athingx.athing.platform.api.message;
    exports io.github.athingx.athing.platform.api.message.decoder;

    requires transitive athing.common;
    requires aliyun.java.sdk.iot;
    requires aliyun.java.sdk.core;

}