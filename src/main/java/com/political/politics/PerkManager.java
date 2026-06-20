package com.political.politics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Government perk selection (zero-sum budget for the Chair, 1-2 for the Vice Chair)
 * and application of perk effects via vanilla attributes and status effects.
 */
public final class PerkManager {

    public static final int CHAIR_MAX_PERKS = 6;
    private static final long EFFECT_DURATION = 600; // ticks; refreshed each perk tick

    private static final Identifier MOD_HEALTH = id("perk_health");
    private static final Identifier MOD_DAMAGE = id("perk_damage");
    private static final Identifier MOD_ARMOR = id("perk_armor");
    private static final Identifier MOD_TOUGH = id("perk_toughness");
    private static final Identifier MOD_SPEED = id("perk_speed");

    private static int tickCounter = 0;

    private PerkManager() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("politicalserver", path);
    }

    public static List<String> activePerks() {
        return DataManager.data().activePerks;
    }

    private static Set<Perk> activeSet() {
        EnumSet<Perk> set = EnumSet.noneOf(Perk.class);
        for (String s : DataManager.data().activePerks) {
            Perk p = Perk.byId(s);
            if (p != null) set.add(p);
        }
        return set;
    }

    /** Chair selection: must sum to exactly 0 points and not exceed the slot cap. */
    public static boolean selectChairPerks(List<Perk> perks) {
        if (perks.size() > CHAIR_MAX_PERKS) return false;
        int sum = 0;
        for (Perk p : perks) sum += p.points;
        if (sum != 0) return false;

        PoliticsData d = DataManager.data();
        d.chairSelectedPerks.clear();
        for (Perk p : perks) d.chairSelectedPerks.add(p.name());
        d.chairPerksSetThisTerm = true;
        rebuildActive();
        return true;
    }

    /** Vice Chair selection: 1-2 perks, none shared with the Chair. */
    public static boolean selectViceChairPerks(List<Perk> perks) {
        if (perks.isEmpty() || perks.size() > 2) return false;
        PoliticsData d = DataManager.data();
        for (Perk p : perks) {
            if (d.chairSelectedPerks.contains(p.name())) return false;
        }
        d.viceChairPerks.clear();
        for (Perk p : perks) d.viceChairPerks.add(p.name());
        d.viceChairPerksSetThisTerm = true;
        rebuildActive();
        return true;
    }

    private static void rebuildActive() {
        PoliticsData d = DataManager.data();
        List<String> active = new ArrayList<>(d.chairSelectedPerks);
        for (String s : d.viceChairPerks) {
            if (!active.contains(s)) active.add(s);
        }
        d.activePerks = active;
    }

    public static void clearAllPerks() {
        PoliticsData d = DataManager.data();
        d.previousTermPerks = new ArrayList<>(d.activePerks);
        d.activePerks.clear();
        d.chairSelectedPerks.clear();
        d.viceChairPerks.clear();
        d.chairPerksSetThisTerm = false;
        d.viceChairPerksSetThisTerm = false;
    }

    public static void applyActivePerks(ServerPlayer player) {
        Set<Perk> active = activeSet();

        double health = 0, damageMult = 0, armor = 0, tough = 0, speedMult = 0;
        for (Perk p : active) {
            if (!p.applied) continue;
            switch (p) {
                case DOUBLE_HEALTH -> health += 1.0;        // +100% of total
                case VOID_TOUCHED -> setAdd(player, MOD_HEALTH, -4);
                case DOUBLE_DAMAGE -> damageMult += 1.0;
                case NATIONAL_UNITY -> damageMult += 0.25;
                case GLASS_CANNON -> damageMult += 0.30;
                case INCREASED_ARMOUR -> armor += 8;
                case FORTIFIED_SHIELDS -> tough += 4;
                case PUBLIC_WORKS -> speedMult += 0.20;
                case INFRASTRUCTURE_NEGLECT -> speedMult -= 0.08;
                default -> { }
            }
        }

        applyMult(player.getAttribute(Attributes.MAX_HEALTH), MOD_HEALTH, health);
        applyMult(player.getAttribute(Attributes.ATTACK_DAMAGE), MOD_DAMAGE, damageMult);
        applyMult(player.getAttribute(Attributes.MOVEMENT_SPEED), MOD_SPEED, speedMult);
        applyAdd(player.getAttribute(Attributes.ARMOR), MOD_ARMOR, armor);
        applyAdd(player.getAttribute(Attributes.ARMOR_TOUGHNESS), MOD_TOUGH, tough);

        if (active.contains(Perk.NIGHTVISION_DECREE)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, (int) EFFECT_DURATION + 20, 0, true, false));
        }
        if (active.contains(Perk.SWIFT_HARVEST)) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, (int) EFFECT_DURATION + 20, 0, true, false));
        }
        if (active.contains(Perk.GOLDEN_AGE)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, (int) EFFECT_DURATION + 20, 0, true, false));
        }
        if (active.contains(Perk.BATTLE_HARDENED)) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, (int) EFFECT_DURATION + 20, 1, true, false));
        }
    }

    private static void applyMult(AttributeInstance inst, Identifier id, double amount) {
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0) {
            inst.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void applyAdd(AttributeInstance inst, Identifier id, double amount) {
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0) {
            inst.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void setAdd(ServerPlayer player, Identifier id, double amount) {
        AttributeInstance inst = player.getAttribute(Attributes.MAX_HEALTH);
        if (inst != null) {
            inst.removeModifier(id);
            inst.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void tickPerks(MinecraftServer server) {
        if (++tickCounter % 100 != 0) return; // every 5s
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            applyActivePerks(player);
        }
    }

    public static Component describeActive() {
        Set<Perk> active = activeSet();
        if (active.isEmpty()) return Component.literal("No active government perks.").withStyle(ChatFormatting.GRAY);
        Component c = Component.literal("Active perks: ").withStyle(ChatFormatting.GOLD);
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Perk p : active) {
            if (!first) sb.append(", ");
            sb.append(p.displayName);
            first = false;
        }
        return c.copy().append(Component.literal(sb.toString()).withStyle(ChatFormatting.YELLOW));
    }
}
