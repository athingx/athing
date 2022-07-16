open module athing.platform.api {

    exports io.github.athingx.athing.platform.api;
    exports io.github.athingx.athing.platform.api.message;
    exports io.github.athingx.athing.platform.api.message.decoder;

    requires transitive athing.common;
    requires iot20180120;

}