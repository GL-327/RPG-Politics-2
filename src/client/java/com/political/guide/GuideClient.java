package com.political.guide;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client wiring for the guide. Registers the {@link GuideOpenS2C} receiver that opens (or
 * refreshes) the {@link GuideScreen}.
 *
 * <p>Integration hook: call {@code GuideClient.registerClient()} from the client entrypoint.
 * It also implements {@link ClientModInitializer} so it can be listed as its own client
 * entrypoint in {@code fabric.mod.json} if preferred.</p>
 */
public final class GuideClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerClient();
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(GuideOpenS2C.TYPE, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreenAndShow(new GuideScreen(payload.chapter()))));
    }
}
