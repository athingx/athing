/**
 * 公共模块
 * <p>
 * 通用工具类、解析类
 * </p>
 */
module athing.common {

    exports io.github.athingx.athing.common;
    exports io.github.athingx.athing.common.gson;
    exports io.github.athingx.athing.common.util;
    requires transitive com.google.gson;
    requires transitive marcono1234.gson.recordadapter;

}