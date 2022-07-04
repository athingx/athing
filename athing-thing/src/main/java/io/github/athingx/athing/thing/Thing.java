package io.github.athingx.athing.thing;

import io.github.athingx.athing.thing.op.ThingOp;

/**
 * 设备
 */
public interface Thing {

    /**
     * 获取设备路径
     *
     * @return 设备路径
     */
    ThingPath path();

    /**
     * 获取设备操作
     *
     * @return 设备操作
     */
    ThingOp op();

    /**
     * 销毁设备
     */
    void destroy();

}
