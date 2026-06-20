package com.political.power;

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
 * The Compound&nbsp;V serum items. Drinking them (right-click) grants powers via
 * {@link Serums}. Registered items so they can be given, looted, and traded like
 * any vanilla item; activation runs server-side through {@link UseItemCallback}.
 */
public final class ModItems {

    public static final String MOD_ID = "politicalserver";

    public static Item COMPOUND_V;
    public static Item TEMP_V;
    public static Item V1;
    public static Item ANTI_V;

    private ModItems() {}

    public static void register() {
        COMPOUND_V = register("compound_v", new Item.Properties().stacksTo(16));
        TEMP_V = register("temp_v", new Item.Properties().stacksTo(16));
        V1 = register("v1", new Item.Properties().stacksTo(16));
        ANTI_V = register("anti_v", new Item.Properties().stacksTo(16));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (stack.is(COMPOUND_V)) {
                Serums.drinkCompoundV(sp);
            } else if (stack.is(TEMP_V)) {
                Serums.drinkTempV(sp);
            } else if (stack.is(V1)) {
                Serums.drinkV1(sp);
            } else if (stack.is(ANTI_V)) {
                Serums.drinkAntiV(sp);
            } else {
                return InteractionResult.PASS;
            }
            if (!sp.isCreative()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        });
    }

    /** Gives one serum item of the given type to the player; returns false if unknown. */
    public static boolean give(ServerPlayer player, String type) {
        Item item = switch (type.toLowerCase()) {
            case "compound", "compound_v" -> COMPOUND_V;
            case "temp", "temp_v" -> TEMP_V;
            case "v1" -> V1;
            case "anti", "anti_v" -> ANTI_V;
            default -> null;
        };
        if (item == null) return false;
        player.getInventory().add(new ItemStack(item));
        return true;
    }

    private static Item register(String name, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(props.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }
}
