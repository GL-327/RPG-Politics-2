package com.political.guide;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The "Field Manual" \u2014 a handheld encyclopedia. Right-clicking it opens the paginated
 * {@link GuideScreen} on the client via a {@link GuideOpenS2C} payload. Mixin-free.
 */
public final class GuideItem {

    public static final String MOD_ID = "politicalserver";
    public static final String ID = "guide_field_manual";

    public static Item FIELD_MANUAL;

    private GuideItem() {}

    static void register() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, ID));
        FIELD_MANUAL = Registry.register(BuiltInRegistries.ITEM, key,
                new Item(new Item.Properties().stacksTo(1).setId(key)));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!sp.getItemInHand(hand).is(FIELD_MANUAL)) return InteractionResult.PASS;
            open(sp, 0);
            return InteractionResult.SUCCESS;
        });
    }

    /** Send the open-guide payload to a player at the given chapter. */
    public static void open(ServerPlayer player, int chapter) {
        com.political.net.ModNetworking.send(player, new GuideOpenS2C(chapter));
    }

    public static ItemStack stack() {
        return new ItemStack(FIELD_MANUAL);
    }
}
