package com.political.dev;

import com.political.net.DevMenuOpenS2C;
import com.political.net.ModNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Creative-only item that opens the Developer Menu (a custom config GUI). */
public final class DevMenuItem {

    public static final String MOD_ID = "politicalserver";
    public static Item DEV_MENU;

    private DevMenuItem() {}

    public static void register() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "dev_menu"));
        DEV_MENU = Registry.register(BuiltInRegistries.ITEM, key,
                new Item(new Item.Properties().stacksTo(1).setId(key)));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!sp.getItemInHand(hand).is(DEV_MENU)) return InteractionResult.PASS;
            if (!isDev(sp)) {
                sp.sendSystemMessage(Component.literal("The Developer Menu is creative/op only.").withStyle(ChatFormatting.RED));
                return InteractionResult.SUCCESS;
            }
            ModNetworking.send(sp, new DevMenuOpenS2C(DevConfig.snapshot()));
            return InteractionResult.SUCCESS;
        });
    }

    public static ItemStack stack() {
        return new ItemStack(DEV_MENU);
    }

    /** The Developer Menu is creative-only. */
    public static boolean isDev(ServerPlayer p) {
        return p.isCreative();
    }
}
