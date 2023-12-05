package io.github.athingx.athing.thing.impl.plugin;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.plugin.ThingPlugin;
import io.github.athingx.athing.thing.api.plugin.ThingPluginInstaller;
import io.github.athingx.athing.thing.api.plugin.ThingPlugins;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ThingPluginsImpl implements ThingPlugins {

    private final Thing thing;
    private final CompletableFuture<Void> destroyF;
    private final Map<String, Stub> stubMap = new ConcurrentHashMap<>();

    public ThingPluginsImpl(Thing thing, CompletableFuture<Void> destroyF) {
        this.thing = thing;
        this.destroyF = destroyF;

        // 注册销毁
        destroyF.whenComplete((unused, cause) -> {
            synchronized (stubMap) {
                stubMap.forEach((identity, stub) -> stub.future().cancel(true));
            }
        });
    }

    @Override
    public <T extends ThingPlugin> CompletableFuture<T> install(ThingPluginInstaller<T> installer) {
        final var identity = installer.meta().identity();
        synchronized (stubMap) {

            // 检查是否已被销毁
            if (destroyF.isDone()) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("thing: %s has been destroyed!".formatted(
                                thing.path()
                        ))
                );
            }

            // 获取或新建插件存根
            final var stub = stubMap.computeIfAbsent(identity, Stub::new);

            // 检查owner是否已经被占用，如果被占用则判定为重复插件
            if (!stub.owner().compareAndSet(null, installer)) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("duplicate plugin identity: %s! existed=%s".formatted(
                                identity,
                                stub.owner().get().meta().type().getName()
                        ))
                );
            }

            // 安装插件
            return installer.install(thing)

                    // 注册插件销毁钩子
                    .whenComplete((plugin, ex) -> destroyF.whenComplete((v, cause) -> plugin.uninstall()))

                    // 安装结果通知结果
                    .whenComplete((plugin, ex) -> {

                        // 安装失败
                        if (null != ex) {
                            stub.future().completeExceptionally(ex);
                            return;
                        }

                        // 安装成功
                        stub.future().complete(plugin);

                    });

        }

    }

    @Override
    public <T extends ThingPlugin> CompletableFuture<T> depends(String identity, Class<T> type) {
        synchronized (stubMap) {

            // 检查是否已被销毁
            if (destroyF.isDone()) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("thing: %s has been destroyed!".formatted(
                                thing.path()
                        ))
                );
            }

            // 从容器中找到对应的插件安装器并返回
            return stubMap.computeIfAbsent(identity, Stub::new)
                    .future()
                    .thenApply(type::cast);
        }
    }

    private record Stub(String identity, AtomicReference<ThingPluginInstaller<?>> owner,
                        CompletableFuture<Object> future) {

        Stub(String identity) {
            this(identity, new AtomicReference<>(), new CompletableFuture<>());
        }

    }

}
