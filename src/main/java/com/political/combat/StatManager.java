package com.political.combat;

import com.political.curse.CursedTrait;
import com.political.court.CourtDomainManager;
import com.political.net.ModNetworking;
import com.political.net.StatSyncS2C;
import com.political.politics.DataManager;
import com.political.power.Power;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Computes and applies RPG stats for 26.2. Health/defense/strength are applied
 * through vanilla attributes (no mixins), and a Skyblock-style action-bar readout
 * plus synced resource values feed the client HUD.
 *
 * <p>There are two distinct resources:
 * <ul>
 *   <li><b>Mana</b> ({@code rpg_intelligence} gear) powers the existing mana items and
 *       all serum (Compound V) powers.</li>
 *   <li><b>Cursed Energy</b> is innate (see {@link CursedTrait}), separate from Mana, and
 *       powers cursed techniques. Most players have very little; some have none at all
 *       (Heavenly Restriction) but gain great physical prowess instead.</li>
 * </ul>
 */
public final class StatManager {

    public static final double BASE_MAX_HEALTH = 100.0;
    public static final double BASE_DEFENSE = 0.0;
    public static final double BASE_STRENGTH = 0.0;
    public static final double BASE_MAX_MANA = 100.0;

    private static final double VANILLA_BASE_HEALTH = 20.0;
    private static final Identifier RPG_HEALTH_ID = Identifier.fromNamespaceAndPath("politicalserver", "rpg_health");
    private static final Identifier RPG_DEFENSE_ID = Identifier.fromNamespaceAndPath("politicalserver", "rpg_defense");
    private static final Identifier RPG_STRENGTH_ID = Identifier.fromNamespaceAndPath("politicalserver", "rpg_strength");
    private static final Identifier RPG_SPEED_ID = Identifier.fromNamespaceAndPath("politicalserver", "rpg_speed");
    private static final Identifier RPG_ATTACK_SPEED_ID = Identifier.fromNamespaceAndPath("politicalserver", "rpg_attack_speed");

    private static final EquipmentSlot[] SCANNED_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    private static final ConcurrentHashMap<UUID, RpgStats> STATS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Double> MANA = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Double> CURSED = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

    private StatManager() {}

    public static RpgStats get(ServerPlayer player) {
        return STATS.computeIfAbsent(player.getUUID(), u -> compute(player));
    }

    // ---------------- Mana ----------------

    public static double getMana(ServerPlayer player) {
        return MANA.getOrDefault(player.getUUID(), BASE_MAX_MANA);
    }

    public static double getMaxMana(ServerPlayer player) {
        return get(player).maxMana;
    }

    public static boolean spendMana(ServerPlayer player, double cost) {
        double current = getMana(player);
        if (current < cost) return false;
        MANA.put(player.getUUID(), current - cost);
        sync(player);
        return true;
    }

    public static void addMana(ServerPlayer player, double amount) {
        MANA.put(player.getUUID(), Math.min(get(player).maxMana, getMana(player) + amount));
        sync(player);
    }

    // ---------------- Cursed Energy ----------------

    public static double getCursedEnergy(ServerPlayer player) {
        return CURSED.getOrDefault(player.getUUID(), 0.0);
    }

    public static double getMaxCursedEnergy(ServerPlayer player) {
        return get(player).maxCursedEnergy;
    }

    public static boolean spendCursedEnergy(ServerPlayer player, double cost) {
        double current = getCursedEnergy(player);
        if (current < cost) return false;
        CURSED.put(player.getUUID(), current - cost);
        sync(player);
        return true;
    }

    /** Adds cursed energy up to the player's maximum; returns the amount actually added. */
    public static double addCursedEnergy(ServerPlayer player, double amount) {
        double max = get(player).maxCursedEnergy;
        double before = getCursedEnergy(player);
        double after = Math.min(max, before + amount);
        CURSED.put(player.getUUID(), after);
        sync(player);
        return after - before;
    }

    // ---------------- Compute ----------------

    public static RpgStats compute(ServerPlayer player) {
        RpgStats s = new RpgStats();
        for (EquipmentSlot slot : SCANNED_SLOTS) {
            addItemStats(s, player.getItemBySlot(slot));
        }
        CursedTrait trait = DataManager.cursedTrait(player.getStringUUID());
        s.maxHealth += trait.bonusHealth;
        s.strength += trait.bonusStrength;
        s.bonusSpeedPct = trait.bonusSpeedPct;
        s.bonusAttackSpeedPct = trait.bonusAttackSpeedPct;
        s.cursedRegenMultiplier = trait.regenMultiplier;
        s.maxCursedEnergy += trait.maxCursedEnergy
                + Power.cursedEnergyBonus(DataManager.knownPowers(player.getStringUUID()));
        return s;
    }

    private static void addItemStats(RpgStats s, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        com.political.items.ItemStats.Sheet sheet = com.political.items.ItemStats.compute(stack);
        s.maxHealth += sheet.health;
        s.defense += sheet.defense;
        s.strength += sheet.strength;
        s.maxMana += sheet.intelligence;
        s.maxCursedEnergy += sheet.cursed;
        s.critChance += sheet.critChance;
        s.critDamage += sheet.critDamage;
        s.ferocity += sheet.ferocity;
        s.speed += sheet.speed;
        s.attackSpeed += sheet.attackSpeed;
    }

    public static void apply(ServerPlayer player) {
        RpgStats s = compute(player);
        STATS.put(player.getUUID(), s);

        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), RPG_HEALTH_ID, s.maxHealth - VANILLA_BASE_HEALTH, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player.getAttribute(Attributes.ARMOR), RPG_DEFENSE_ID, s.defense * 0.15, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), RPG_STRENGTH_ID, s.strength * 0.1, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), RPG_SPEED_ID, s.bonusSpeedPct + s.speed * 0.005, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(player.getAttribute(Attributes.ATTACK_SPEED), RPG_ATTACK_SPEED_ID, s.bonusAttackSpeedPct + s.attackSpeed * 0.01, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        MANA.put(player.getUUID(), Math.min(MANA.getOrDefault(player.getUUID(), s.maxMana), s.maxMana));
        CURSED.put(player.getUUID(), Math.min(CURSED.getOrDefault(player.getUUID(), 0.0), s.maxCursedEnergy));
        sync(player);
    }

    private static void applyModifier(AttributeInstance instance, Identifier id, double amount, AttributeModifier.Operation op) {
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, op));
        }
    }

    public static void sync(ServerPlayer player) {
        RpgStats s = get(player);
        ModNetworking.send(player, new StatSyncS2C(
                (float) s.defense, (float) s.strength,
                (float) s.maxMana, (float) getMana(player),
                (float) s.maxCursedEnergy, (float) getCursedEnergy(player)));
    }

    public static void tickAll(MinecraftServer server) {
        tickCounter++;
        boolean secondTick = (tickCounter % 20 == 0);
        if (!secondTick) return;

        boolean courtActive = CourtDomainManager.isActive();
        double manaRate = DataManager.data().manaRegenRate;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            apply(player);
            RpgStats s = get(player);

            double mana = getMana(player);
            if (mana < s.maxMana) {
                MANA.put(player.getUUID(), Math.min(s.maxMana, mana + s.maxMana * manaRate));
            }
            double cursed = getCursedEnergy(player);
            if (s.maxCursedEnergy > 0 && cursed < s.maxCursedEnergy && s.cursedRegenMultiplier > 0) {
                int grade = DataManager.sorcererGrade(player.getStringUUID());
                double rate = 0.015 * s.cursedRegenMultiplier * (1.0 + grade * 0.12);
                CURSED.put(player.getUUID(), Math.min(s.maxCursedEnergy, cursed + s.maxCursedEnergy * rate));
            }

            if (!courtActive) {
                sendStatActionBar(player, s);
            }
        }
    }

    private static void sendStatActionBar(ServerPlayer player, RpgStats s) {
        int hp = (int) Math.ceil(player.getHealth());
        int maxHp = (int) Math.round(player.getMaxHealth());
        int mana = (int) Math.floor(getMana(player));
        Component bar = Component.literal("\u2764 " + hp + "/" + maxHp + "   ").withStyle(ChatFormatting.RED)
                .append(Component.literal("\u2748 " + (int) s.defense + " Def   ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal("\u2726 " + (int) s.strength + " Str   ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("\u2742 " + mana + "/" + (int) s.maxMana + " Mana").withStyle(ChatFormatting.AQUA));
        if (s.maxCursedEnergy > 0) {
            int ce = (int) Math.floor(getCursedEnergy(player));
            bar = bar.copy().append(Component.literal("   \u2620 " + ce + "/" + (int) s.maxCursedEnergy + " CE")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (s.critChance > 0) {
            bar = bar.copy().append(Component.literal("   \u2741 " + (int) s.critChance + "% Crit")
                    .withStyle(ChatFormatting.BLUE));
        }
        if (s.ferocity > 0) {
            bar = bar.copy().append(Component.literal("   \u2694 " + (int) s.ferocity + " Fer")
                    .withStyle(ChatFormatting.RED));
        }
        player.sendSystemMessage(bar, true);
    }

    public static void remove(UUID uuid) {
        STATS.remove(uuid);
        MANA.remove(uuid);
        CURSED.remove(uuid);
    }
}
