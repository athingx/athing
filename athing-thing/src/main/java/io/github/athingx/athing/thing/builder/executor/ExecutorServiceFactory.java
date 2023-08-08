package io.github.athingx.athing.thing.builder.executor;

import io.github.athingx.athing.thing.api.ThingPath;

import java.util.concurrent.ExecutorService;

/**
 * 线程池工厂
 */
public interface ExecutorServiceFactory {

    /**
     * 创建线程池
     *
     * @param path 设备路径
     * @return 线程池
     */
    ExecutorService make(ThingPath path);

}
