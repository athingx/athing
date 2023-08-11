package io.github.athingx.athing.thing.api.op;

import com.google.gson.annotations.SerializedName;

import static io.github.athingx.athing.thing.api.op.OpRequest.Ext.ACK;

public record OpRequest<T>(
        @SerializedName("id") String token,
        @SerializedName("version") String version,
        @SerializedName("method") String method,
        @SerializedName("sys") Ext ext,
        @SerializedName("params") T params
) {

    public static final String DEFAULT_VERSION = "1.0";

    public OpRequest(String token, String method, T params) {
        this(token, DEFAULT_VERSION, method, new Ext(ACK), params);
    }

    public record Ext(int ack) {
        public static final int NO_ACK = 0;
        public static final int ACK = 1;
    }

}
