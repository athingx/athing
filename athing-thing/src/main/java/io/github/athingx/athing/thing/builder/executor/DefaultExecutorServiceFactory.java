package io.github.athingx.athing.thing.builder.executor;

import io.github.athingx.athing.thing.api.ThingPath;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认线程池工厂
 */
public class DefaultExecutorServiceFactory implements ExecutorServiceFactory {

    private final AtomicInteger counter = new AtomicInteger(1000);
    private final int nThreads;

    /**
     * 创建默认线程池工厂
     *
     * @param nThreads 线程数
     */
    public DefaultExecutorServiceFactory(int nThreads) {
        this.nThreads = nThreads;
    }

    /**
     * 创建默认线程池工厂
     */
    public DefaultExecutorServiceFactory() {
        this(Math.max(16, Runtime.getRuntime().availableProcessors() * 4));
    }

    @Override
    public ExecutorService make(ThingPath path) {
        return Executors.newFixedThreadPool(nThreads, r -> new Thread(r) {{
            setDaemon(true);
            setName("athing-executor-%s-%d".formatted(path, counter.incrementAndGet()));
        }});
    }

}
