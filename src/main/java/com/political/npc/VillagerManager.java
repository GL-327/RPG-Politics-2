package com.political.npc;

import com.political.combat.StatManager;
import com.political.items.RpgItem;
import com.political.items.RpgItems;
import com.political.politics.DataManager;
import com.political.power.Power;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * RPG villagers: stable names, roles, citizenship — dialogue handled by {@link DialogueManager}.
 */
public final class VillagerManager {

    private static final Random RNG = new Random();
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private static final String[] NAMES = {
            "Aldric", "Bryn", "Cassia", "Doran", "Elowen", "Fenwick", "Greta", "Halden",
            "Isolde", "Joran", "Katla", "Lorne", "Mirela", "Nestor", "Orla", "Perrin",
            "Quill", "Rowan", "Sable", "Tamsin", "Ulric", "Vesna", "Wren", "Yorick"
    };

    public enum Role {
        HEALER("Healer", ChatFormatting.AQUA),
        SAGE("Sage", ChatFormatting.LIGHT_PURPLE),
        BLACKSMITH("Blacksmith", ChatFormatting.GOLD),
        MERCHANT("Merchant", ChatFormatting.GREEN),
        GUARD("Guard", ChatFormatting.RED);

        public final String title;
        public final ChatFormatting color;

        Role(String title, ChatFormatting color) {
            this.title = title;
            this.color = color;
        }
    }

    private VillagerManager() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Villager villager && !villager.hasCustomName()) {
                nameVillager(villager);
                enroll(villager, world);
            }
        });
    }

    public static Role roleOf(UUID id) {
        Role[] roles = Role.values();
        return roles[Math.floorMod(id.hashCode(), roles.length)];
    }

    public static String nameOf(UUID id) {
        return NAMES[Math.floorMod(id.hashCode() >> 8, NAMES.length)];
    }

    private static void nameVillager(Villager villager) {
        Role role = roleOf(villager.getUUID());
        String name = nameOf(villager.getUUID());
        villager.setCustomName(Component.literal(name + " the " + role.title).withStyle(role.color));
        villager.setCustomNameVisible(false);
    }

    private static void enroll(Villager villager, ServerLevel level) {
        com.political.politics.Settlement s = com.political.world.SettlementManager.nearestSettlement(
                level, villager.getX(), villager.getZ());
        if (s == null) return;
        DataManager.setCitizenship(villager.getStringUUID(), s.id);
        if (DataManager.civicRank(villager.getStringUUID()).ordinal() == 0) {
            DataManager.setCivicRank(villager.getStringUUID(), com.political.politics.CivicRank.CITIZEN);
        }
    }

    private static boolean offCooldown(Villager v, ServerPlayer p, String service, long ms) {
        String key = v.getStringUUID() + "|" + p.getStringUUID() + "|" + service;
        long now = System.currentTimeMillis();
        Long ready = COOLDOWNS.get(key);
        if (ready != null && now < ready) return false;
        COOLDOWNS.put(key, now + ms);
        return true;
    }

    public static void runHealer(ServerPlayer p, Villager v, String name) {
        int cost = 40;
        if (p.getHealth() >= p.getMaxHealth() && StatManager.getMana(p) >= StatManager.getMaxMana(p)) return;
        if (!offCooldown(v, p, "heal", 30_000L)) return;
        if (!DataManager.removeCoins(p.getStringUUID(), cost)) return;
        p.setHealth(p.getMaxHealth());
        StatManager.addMana(p, StatManager.getMaxMana(p));
        StatManager.addCursedEnergy(p, StatManager.getMaxCursedEnergy(p));
        p.removeEffect(MobEffects.POISON);
        p.removeEffect(MobEffects.WITHER);
    }

    public static void runSage(ServerPlayer p, Villager v, String name) {
        String uuid = p.getStringUUID();
        if (DataManager.sorcererGrade(uuid) < 1) return;
        int cost = 5;
        List<Power> teachable = Power.ofOrigin(Power.Origin.CURSED_TECHNIQUE);
        teachable.removeIf(t -> DataManager.hasPower(uuid, t.id()));
        if (teachable.isEmpty()) return;
        Power pick = teachable.get(RNG.nextInt(teachable.size()));
        if (!DataManager.removeCredits(uuid, cost)) return;
        DataManager.grantPower(uuid, pick.id());
    }

    public static void runBlacksmith(ServerPlayer p, Villager v, String name) {
        RpgItem[] all = RpgItem.values();
        RpgItem pick = all[RNG.nextInt(all.length)];
        int cost = priceOf(pick.rarity);
        if (!offCooldown(v, p, "smith", 5_000L)) return;
        if (!DataManager.removeCoins(p.getStringUUID(), cost)) return;
        ItemStack stack = RpgItems.create(pick);
        if (!p.getInventory().add(stack)) p.drop(stack, false);
    }

    public static void runMerchant(ServerPlayer p, Villager v, String name) {
        if (!offCooldown(v, p, "trade", 10 * 60_000L)) return;
        int gift = 25 + RNG.nextInt(50);
        DataManager.addCoins(p.getStringUUID(), gift);
    }

    public static void runGuard(ServerPlayer p, Villager v, String name) {
        int cost = 30;
        if (!offCooldown(v, p, "bless", 60_000L)) return;
        if (!DataManager.removeCoins(p.getStringUUID(), cost)) return;
        p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200, 1, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1200, 0, false, true));
    }

    private static int priceOf(com.political.items.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 80;
            case UNCOMMON -> 160;
            case RARE -> 320;
            case EPIC -> 700;
            case LEGENDARY -> 1500;
            case MYTHIC -> 3000;
        };
    }
}
