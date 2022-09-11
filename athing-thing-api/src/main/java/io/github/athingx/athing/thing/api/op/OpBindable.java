package io.github.athingx.athing.thing.api.op;

/**
 * 可绑定的
 */
public interface OpBindable {

    /**
     * 操作绑定
     *
     * @param express 绑定主题表达式
     * @return 函数绑定
     */
    OpBinding<byte[]> binding(String express);

}
