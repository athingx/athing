package io.github.athingx.athing.platform.builder.client;

import io.github.athingx.athing.platform.api.client.ThingPlatformClient;

public interface ThingPlatformClientFactory {

    ThingPlatformClient make() throws Exception;

}
