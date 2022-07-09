package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.OpBind;
import io.github.athingx.athing.thing.api.op.OpData;
import io.github.athingx.athing.thing.api.op.OpGroupBind;
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
    public CompletableFuture<Void> data(String topic, OpData data) {
        return linker.data(topic, data);
    }

    @Override
    public OpBind<byte[]> bind(String express) {
        return linker.bind(express);
    }

    @Override
    public OpGroupBind group() {
        return linker.group();
    }


}
