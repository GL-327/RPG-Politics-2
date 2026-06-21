package com.political.world.dungeons;

import com.political.RpgPoliticsMod;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** RPG-flavoured compass that points toward the nearest dungeon structure. */
public final class StructureCompassItem extends Item {

    public static Item STRUCTURE_COMPASS;

    public StructureCompassItem(Properties props) {
        super(props);
    }

    public static void register() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, "structure_compass"));
        STRUCTURE_COMPASS = Registry.register(BuiltInRegistries.ITEM, key,
                new StructureCompassItem(new Properties().setId(key).stacksTo(1)));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            if (!stack.is(STRUCTURE_COMPASS)) return InteractionResult.PASS;
            if (!(sp.level() instanceof ServerLevel level)) return InteractionResult.PASS;
            DungeonSite site = DungeonManager.nearest(level, sp.getX(), sp.getZ());
            if (site == null) {
                sp.sendSystemMessage(Component.literal("The compass needle spins wildly — no structures nearby.")
                        .withStyle(ChatFormatting.GRAY));
                return InteractionResult.SUCCESS;
            }
            int dx = site.x - sp.getBlockX();
            int dz = site.z - sp.getBlockZ();
            String dir = compassDir(dx, dz);
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            sp.sendSystemMessage(Component.literal("Structure Compass: " + site.type.display
                    + " ~" + dist + "m " + dir).withStyle(site.type.color));
            sp.sendSystemMessage(Component.literal("Coords: " + site.x + ", " + site.y + ", " + site.z)
                    .withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResult.SUCCESS;
        });
    }

    private static String compassDir(int dx, int dz) {
        if (Math.abs(dx) > Math.abs(dz) * 2) return dx > 0 ? "east" : "west";
        if (Math.abs(dz) > Math.abs(dx) * 2) return dz > 0 ? "south" : "north";
        if (dx > 0 && dz > 0) return "southeast";
        if (dx > 0) return "northeast";
        if (dz > 0) return "southwest";
        return "northwest";
    }

    public static ItemStack stack() {
        return new ItemStack(STRUCTURE_COMPASS != null ? STRUCTURE_COMPASS : Items.COMPASS);
    }
}
