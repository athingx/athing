package io.github.athingx.athing.thing.op;

import java.util.concurrent.CompletableFuture;

public interface OpGroupBind {

    <V> OpBind<V> bind(String express);

    CompletableFuture<OpBinder> bind();

}
