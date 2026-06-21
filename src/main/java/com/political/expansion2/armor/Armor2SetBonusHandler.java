package com.political.expansion2.armor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class Armor2SetBonusHandler {
    private static final int INTERVAL = 20;
    private static final EquipmentSlot[] ARMOR = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    private Armor2SetBonusHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % INTERVAL == 0) tick(server);
        });
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ArmorSet worn = wornSet(player);
            if (worn != null) worn.bonus.apply(player);
        }
    }

    private static ArmorSet wornSet(ServerPlayer player) {
        String setKey = null;
        for (EquipmentSlot slot : ARMOR) {
            String key = Armor2Items.setKeyOf(player.getItemBySlot(slot));
            if (key == null) return null;
            if (setKey == null) setKey = key;
            else if (!setKey.equals(key)) return null;
        }
        if (setKey == null) return null;
        for (ArmorSet set : ArmorSet.values()) if (set.key.equals(setKey)) return set;
        return null;
    }
}
