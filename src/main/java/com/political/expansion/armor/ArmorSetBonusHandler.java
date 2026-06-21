package com.political.expansion.armor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Self-contained full-set-bonus engine for the {@code arm_} armour expansion.
 *
 * <p>This intentionally does <b>not</b> edit the shared {@link com.political.combat.StatManager} or
 * {@link com.political.combat.AbilityEngine}. It registers its own server-tick listener, reads each
 * player's four worn armour pieces, and — when all four belong to the same {@link ArmorSet} — applies
 * that set's {@link ArmorSet.SetEffect}. Per-piece passive abilities are still handled by the existing
 * {@code AbilityEngine} equipment tick; this only layers the extra full-set bonus on top.
 */
public final class ArmorSetBonusHandler {

    /** Run roughly once per second, like the other periodic gear ticks. */
    private static final int INTERVAL = 20;

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ArmorSetBonusHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % INTERVAL == 0) {
                tick(server);
            }
        });
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ArmorSet worn = wornSet(player);
            if (worn != null) {
                worn.bonus.apply(player);
            }
        }
    }

    /** Returns the set the player is wearing all four pieces of, or {@code null}. */
    private static ArmorSet wornSet(ServerPlayer player) {
        String setKey = null;
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = player.getItemBySlot(slot);
            String key = ArmorItems.setKeyOf(stack);
            if (key == null) return null;
            if (setKey == null) {
                setKey = key;
            } else if (!setKey.equals(key)) {
                return null;
            }
        }
        if (setKey == null) return null;
        for (ArmorSet set : ArmorSet.values()) {
            if (set.key.equals(setKey)) return set;
        }
        return null;
    }
}
