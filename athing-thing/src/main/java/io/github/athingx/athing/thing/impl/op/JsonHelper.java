package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.op.OpReply;

import java.util.Objects;

class JsonHelper {

    public static String toJson(Object object) {

        final Object data;
        if (object instanceof OpReply<?> reply) {
            data = new OpReply<>(
                    reply.token(),
                    reply.code(),
                    reply.desc(),
                    Objects.requireNonNullElse(reply.data(), new Object())
            );
        } else {
            data = object;
        }

        return GsonFactory.getGson().toJson(data);
    }

}
