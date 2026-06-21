package com.political.expansion2.npc;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Expansion2VillagerHooks {

    private static final Map<UUID, NpcArchetype> ARCHETYPES = new ConcurrentHashMap<>();
    private static final String[] NAMES = {
            "Aldric", "Bryn", "Cassia", "Doran", "Elowen", "Fenwick", "Greta", "Halden",
            "Isolde", "Joran", "Katla", "Lorne", "Mirela", "Nestor", "Orla", "Perrin",
            "Quill", "Rowan", "Sable", "Tamsin", "Ulric", "Vesna", "Wren", "Yorick",
            "Xander", "Zelda", "Bran", "Cyra", "Dax", "Elara", "Finn", "Gideon"
    };

    private Expansion2VillagerHooks() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Villager villager) tag(villager);
        });
    }

    public static void tag(Villager villager) {
        UUID id = villager.getUUID();
        NpcArchetype arch = NpcArchetype.forVillager(id);
        ARCHETYPES.put(id, arch);
        villager.setCustomName(Component.literal(nameOf(id) + " the " + arch.title).withStyle(arch.color));
        villager.setCustomNameVisible(false);
    }

    public static NpcArchetype archetypeOf(UUID id) {
        return ARCHETYPES.getOrDefault(id, NpcArchetype.forVillager(id));
    }

    public static String nameOf(UUID id) {
        return NAMES[Math.floorMod(id.hashCode() >> 8, NAMES.length)];
    }

    public static boolean isExpansionNpc(UUID villagerId) {
        return ARCHETYPES.containsKey(villagerId);
    }
}
