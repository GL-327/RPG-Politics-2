package com.political.guide;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Common (client + server) entrypoint for the in-game guide / encyclopedia.
 *
 * <p>Integration hook: call {@code GuideRegistry.register()} once from the mod's
 * {@code onInitialize()}. This self-contains the item, the open payload, the {@code /guide}
 * command, and a creative tab so no other package needs editing. The client receiver is wired
 * separately via {@code GuideClient.registerClient()} from the client entrypoint.</p>
 */
public final class GuideRegistry {

    public static final String MOD_ID = "politicalserver";

    private GuideRegistry() {}

    public static void register() {
        GuideItem.register();

        PayloadTypeRegistry.clientboundPlay().register(GuideOpenS2C.TYPE, GuideOpenS2C.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GuideCommands.register(dispatcher));

        registerCreativeTab();
    }

    /** A dedicated creative tab holding the Field Manual, so it is always reachable in creative. */
    private static void registerCreativeTab() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 8)
                .title(Component.translatable("itemGroup." + MOD_ID + ".guide"))
                .icon(GuideItem::stack)
                .displayItems((params, out) -> out.accept(GuideItem.stack()))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(MOD_ID, "guide"), tab);
    }
}
