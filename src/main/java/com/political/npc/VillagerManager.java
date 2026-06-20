package com.political.npc;

import com.political.combat.StatManager;
import com.political.items.RpgItem;
import com.political.items.RpgItems;
import com.political.politics.DataManager;
import com.political.power.Power;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
 * Turns vanilla villagers into light-weight RPG NPCs: each gets a stable name and an
 * RPG role (Healer, Sage, Blacksmith, Merchant, Guard) derived from its UUID. Right-click
 * for the role's service; sneak + right-click still opens normal trades. No GUIs/mixins \u2014
 * services run through chat + the existing economy and power systems.
 */
public final class VillagerManager {

    private static final Random RNG = new Random();
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private static final String[] NAMES = {
            "Aldric", "Bryn", "Cassia", "Doran", "Elowen", "Fenwick", "Greta", "Halden",
            "Isolde", "Joran", "Katla", "Lorne", "Mirela", "Nestor", "Orla", "Perrin",
            "Quill", "Rowan", "Sable", "Tamsin", "Ulric", "Vesna", "Wren", "Yorick"
    };

    private enum Role {
        HEALER("Healer", ChatFormatting.AQUA),
        SAGE("Sage", ChatFormatting.LIGHT_PURPLE),
        BLACKSMITH("Blacksmith", ChatFormatting.GOLD),
        MERCHANT("Merchant", ChatFormatting.GREEN),
        GUARD("Guard", ChatFormatting.RED);

        final String title;
        final ChatFormatting color;

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
            }
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(entity instanceof Villager villager) || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (sp.isShiftKeyDown()) return InteractionResult.PASS; // let vanilla trades open
            if (!villager.hasCustomName()) nameVillager(villager);
            interact(sp, villager);
            return InteractionResult.SUCCESS;
        });
    }

    private static Role roleOf(UUID id) {
        Role[] roles = Role.values();
        return roles[Math.floorMod(id.hashCode(), roles.length)];
    }

    private static String nameOf(UUID id) {
        return NAMES[Math.floorMod(id.hashCode() >> 8, NAMES.length)];
    }

    private static void nameVillager(Villager villager) {
        Role role = roleOf(villager.getUUID());
        String name = nameOf(villager.getUUID());
        villager.setCustomName(Component.literal(name + " the " + role.title).withStyle(role.color));
        villager.setCustomNameVisible(true);
    }

    private static boolean offCooldown(Villager v, ServerPlayer p, String service, long ms) {
        String key = v.getStringUUID() + "|" + p.getStringUUID() + "|" + service;
        long now = System.currentTimeMillis();
        Long ready = COOLDOWNS.get(key);
        if (ready != null && now < ready) return false;
        COOLDOWNS.put(key, now + ms);
        return true;
    }

    private static void interact(ServerPlayer p, Villager villager) {
        Role role = roleOf(villager.getUUID());
        String name = nameOf(villager.getUUID());
        switch (role) {
            case HEALER -> healer(p, villager, name);
            case SAGE -> sage(p, villager, name);
            case BLACKSMITH -> blacksmith(p, villager, name);
            case MERCHANT -> merchant(p, villager, name);
            case GUARD -> guard(p, villager, name);
        }
    }

    private static void say(ServerPlayer p, String name, String text, ChatFormatting color) {
        p.sendSystemMessage(Component.literal("[" + name + "] ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(text).withStyle(color)));
    }

    private static void healer(ServerPlayer p, Villager v, String name) {
        int cost = 40;
        if (p.getHealth() >= p.getMaxHealth() && StatManager.getEnergy(p) >= StatManager.getMaxEnergy(p)) {
            say(p, name, "You are already in fine health, traveller.", Role.HEALER.color);
            return;
        }
        if (!offCooldown(v, p, "heal", 30_000L)) {
            say(p, name, "Rest a moment before I tend you again.", ChatFormatting.GRAY);
            return;
        }
        if (!DataManager.removeCoins(p.getStringUUID(), cost)) {
            say(p, name, "Healing costs " + cost + " coins, and your purse is light.", ChatFormatting.RED);
            return;
        }
        p.setHealth(p.getMaxHealth());
        StatManager.addEnergy(p, StatManager.getMaxEnergy(p));
        p.removeEffect(MobEffects.POISON);
        p.removeEffect(MobEffects.WITHER);
        say(p, name, "Be whole again. (-" + cost + " coins)", Role.HEALER.color);
    }

    private static void sage(ServerPlayer p, Villager v, String name) {
        String uuid = p.getStringUUID();
        if (DataManager.sorcererGrade(uuid) < 1) {
            say(p, name, "I sense no cursed energy in you. Awaken it with /cursed awaken, then return.", Role.SAGE.color);
            return;
        }
        int cost = 5; // credits
        List<Power> teachable = Power.ofOrigin(Power.Origin.CURSED_TECHNIQUE);
        teachable.removeIf(t -> DataManager.hasPower(uuid, t.id()));
        if (teachable.isEmpty()) {
            say(p, name, "You have learned all I can teach. Impressive.", Role.SAGE.color);
            return;
        }
        Power pick = teachable.get(RNG.nextInt(teachable.size()));
        if (!DataManager.removeCredits(uuid, cost)) {
            say(p, name, "My teachings cost " + cost + " credits. Earn more and return.", ChatFormatting.RED);
            return;
        }
        DataManager.grantPower(uuid, pick.id());
        say(p, name, "I impart the technique: " + pick.displayName + ". (-" + cost + " credits)", Role.SAGE.color);
    }

    private static void blacksmith(ServerPlayer p, Villager v, String name) {
        RpgItem[] all = RpgItem.values();
        RpgItem pick = all[RNG.nextInt(all.length)];
        int cost = priceOf(pick.rarity);
        if (!offCooldown(v, p, "smith", 5_000L)) {
            say(p, name, "Patience \u2014 the forge runs hot.", ChatFormatting.GRAY);
            return;
        }
        if (!DataManager.removeCoins(p.getStringUUID(), cost)) {
            say(p, name, "Today's wares: " + pick.displayName + " for " + cost + " coins. Come back with gold.", Role.BLACKSMITH.color);
            return;
        }
        ItemStack stack = RpgItems.create(pick);
        if (!p.getInventory().add(stack)) p.drop(stack, false);
        say(p, name, "A fine choice: " + pick.displayName + ". (-" + cost + " coins)", Role.BLACKSMITH.color);
    }

    private static void merchant(ServerPlayer p, Villager v, String name) {
        if (!offCooldown(v, p, "trade", 10 * 60_000L)) {
            say(p, name, "I've no new deals just now. Return later.", ChatFormatting.GRAY);
            return;
        }
        int gift = 25 + RNG.nextInt(50);
        DataManager.addCoins(p.getStringUUID(), gift);
        say(p, name, "Good fortune to you, friend \u2014 a token of goodwill. (+" + gift + " coins)", Role.MERCHANT.color);
    }

    private static void guard(ServerPlayer p, Villager v, String name) {
        int cost = 30;
        if (!offCooldown(v, p, "bless", 60_000L)) {
            say(p, name, "My blessing still lingers on you.", ChatFormatting.GRAY);
            return;
        }
        if (!DataManager.removeCoins(p.getStringUUID(), cost)) {
            say(p, name, "A warrior's blessing costs " + cost + " coins.", ChatFormatting.RED);
            return;
        }
        p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200, 1, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1200, 0, false, true));
        say(p, name, "Go forth with strength and a guarded heart. (-" + cost + " coins)", Role.GUARD.color);
    }

    private static int priceOf(RpgItem.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 80;
            case RARE -> 320;
            case EPIC -> 700;
            case LEGENDARY -> 1500;
            case MYTHIC -> 3000;
        };
    }
}
