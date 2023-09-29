package io.github.athingx.athing.thing;

import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.op.OpReply;
import org.junit.Test;

public class OpReplyTestCase {

    @Test
    public void test$op_reply$decode() {

        final var reply =
                // OpReply.succeed("token", "test-data")
                OpReply.fail("token", 400, "test-error")
                ;
        final var json = GsonFactory.getGson().toJson(reply);
        System.out.println(json);

        final var clone = GsonFactory.getGson().fromJson(json, OpReply.class);
        System.out.println(clone);

    }

}
