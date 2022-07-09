package io.github.athingx.athing.thing.builder;

import io.github.athingx.athing.thing.api.ThingPath;

import java.util.concurrent.ExecutorService;

/**
 * Executor工厂
 */
public interface ExecutorServiceFactory {

    /**
     * 生产Executor
     *
     * @param path 设备路径
     * @return Executor
     */
    ExecutorService make(ThingPath path);

}
