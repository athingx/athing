package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.OpBatchBinding;
import io.github.athingx.athing.thing.api.op.OpBinding;
import io.github.athingx.athing.thing.api.op.OpData;
import io.github.athingx.athing.thing.api.op.ThingOp;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ThingOpImpl implements ThingOp {

    private final ThingOpLinker linker;

    public ThingOpImpl(ThingPath path, IMqttAsyncClient client, ExecutorService executor) {
        this.linker = new ThingOpLinker(path, client, executor);
    }

    @Override
    public String genToken() {
        return linker.genToken();
    }

    @Override
    public CompletableFuture<Void> post(String topic, OpData data) {
        return linker.post(topic, data);
    }

    @Override
    public OpBinding<byte[]> binding(String express) {
        return linker.binding(express);
    }

    @Override
    public OpBatchBinding binding() {
        return linker.binding();
    }


}
