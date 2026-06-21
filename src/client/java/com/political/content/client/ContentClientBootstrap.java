package com.political.content.client;

import com.political.content.creatures.client.ContentCreaturesClient;

/**
 * Client-side counterpart to {@link com.political.content.ContentBootstrap}. Registers the content
 * workstream's entity models and renderers. Call once from the client initializer:
 *
 * <pre>
 *   com.political.content.client.ContentClientBootstrap.initClient();
 * </pre>
 */
public final class ContentClientBootstrap {

    private ContentClientBootstrap() {}

    public static void initClient() {
        ContentCreaturesClient.registerClient();
    }
}
