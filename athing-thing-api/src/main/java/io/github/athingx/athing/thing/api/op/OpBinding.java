package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

public interface OpBinding<T extends OpBinder> {

    CompletableFuture<T> binding(OpGroupBind group);

}
