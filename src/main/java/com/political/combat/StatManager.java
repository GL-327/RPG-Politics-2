package com.political.combat;

import com.political.curse.CursedTrait;
import com.political.curse.SorcererGrade;
import com.political.net.ModNetworking;
import com.political.net.StatSyncS2C;
import com.political.politics.DataManager;
import com.political.power.Power;
import net.minecraft.core.component.DataComponents;
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
        setCursed(player, current - cost);
        return true;
    }

    /** Adds cursed energy up to the player's maximum; returns the amount actually added. */
    public static double addCursedEnergy(ServerPlayer player, double amount) {
        double max = get(player).maxCursedEnergy;
        double before = getCursedEnergy(player);
        double after = Math.min(max, before + amount);
        setCursed(player, after);
        return after - before;
    }

    /** Refills a player's cursed energy to their maximum (used when awakening). */
    public static void refillCursedEnergy(ServerPlayer player) {
        setCursed(player, get(player).maxCursedEnergy);
    }

    private static void setCursed(ServerPlayer player, double value) {
        CURSED.put(player.getUUID(), value);
        DataManager.setStoredCursedEnergy(player.getStringUUID(), value);
        sync(player);
    }

    // ---------------- Compute ----------------

    public static RpgStats compute(ServerPlayer player) {
        RpgStats s = new RpgStats();
        for (EquipmentSlot slot : SCANNED_SLOTS) {
            addItemStats(s, player.getItemBySlot(slot));
        }
        s.strength += com.political.expansion.accessories.Accessories.strengthBonus(player);
        s.strength += com.political.expansion2.accessories.Accessories2.strengthBonus(player);
        CursedTrait trait = DataManager.cursedTrait(player.getStringUUID());
        s.maxHealth += trait.bonusHealth;
        s.strength += trait.bonusStrength;
        s.bonusSpeedPct = trait.bonusSpeedPct;
        s.bonusAttackSpeedPct = trait.bonusAttackSpeedPct;
        s.cursedRegenMultiplier = trait.regenMultiplier;
        s.maxCursedEnergy += trait.maxCursedEnergy
                + Power.cursedEnergyBonus(DataManager.knownPowers(player.getStringUUID()))
                + com.political.expansion2.powers.Power2.cursedEnergyBonus(DataManager.knownPowers(player.getStringUUID()));
        int grade = DataManager.sorcererGrade(player.getStringUUID());
        if (grade > 0) {
            s.maxCursedEnergy = Math.max(s.maxCursedEnergy, SorcererGrade.maxCursedEnergyFor(grade));
        }
        String presetId = com.political.curse.limb.LimbStateManager.presetId(player);
        s.maxCursedEnergy += com.political.curse.jjk.JjkPresetRegistry.bonusMaxCe(presetId);
        s.cursedRegenMultiplier *= com.political.curse.jjk.JjkPresetRegistry.regenMultiplier(presetId);
        // Sworn binding vows levy a permanent toll on the body while in effect (never below 1 heart worth).
        double vowHealthDelta = com.political.curse.rules.JjkRules.flatHealthDelta(player.getStringUUID());
        if (vowHealthDelta != 0) {
            s.maxHealth = Math.max(VANILLA_BASE_HEALTH, s.maxHealth + vowHealthDelta);
        }
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
        // sheet.damage is consumed by AbilityEngine's Skyblock hit formula via ItemStats.compute(weapon)
    }

    public static void apply(ServerPlayer player) {
        RpgStats s = compute(player);
        STATS.put(player.getUUID(), s);

        // Conservative safety clamps so extreme gear/multiplier stacking can't exceed vanilla
        // attribute bounds or trivialise damage. Defaults are far above any real catalogue value.
        com.political.config.PoliticalConfig cfg = com.political.config.PoliticalConfig.get();
        s.maxHealth = Math.min(s.maxHealth, cfg.maxPlayerHealth);
        s.defense = Math.min(s.defense, cfg.maxPlayerDefense);

        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), RPG_HEALTH_ID, s.maxHealth - VANILLA_BASE_HEALTH, AttributeModifier.Operation.ADD_VALUE);
        // Skyblock defense reduces damage by defense/(defense+K). Vanilla armour gives ~4% per point
        // (capped at 80% / 20 points), so armour = 25 * reduction reproduces that EHP curve exactly.
        applyModifier(player.getAttribute(Attributes.ARMOR), RPG_DEFENSE_ID, armorFromDefense(s.defense, cfg.defenseEhpConstant), AttributeModifier.Operation.ADD_VALUE);
        // Strength feeds the Skyblock damage formula, not vanilla attack damage.
        applyModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), RPG_STRENGTH_ID, 0, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), RPG_SPEED_ID, s.bonusSpeedPct + s.speed * 0.005, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(player.getAttribute(Attributes.ATTACK_SPEED), RPG_ATTACK_SPEED_ID, 2.0 + s.bonusAttackSpeedPct, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        MANA.put(player.getUUID(), Math.min(MANA.getOrDefault(player.getUUID(), s.maxMana), s.maxMana));
        // Load persisted cursed energy on first apply (e.g. after relog), then clamp to max.
        double curStored = CURSED.containsKey(player.getUUID())
                ? CURSED.get(player.getUUID())
                : DataManager.storedCursedEnergy(player.getStringUUID());
        CURSED.put(player.getUUID(), Math.min(curStored, s.maxCursedEnergy));
        sync(player);
    }

    /**
     * Converts Skyblock Defense into vanilla armour points so incoming damage is reduced by
     * {@code defense/(defense+K)} (the Skyblock EHP curve) without any mixin on the hurt path.
     * Vanilla armour reduces by 4% per point up to 80% at 20 points, so {@code armour = 25 * r}.
     */
    public static double armorFromDefense(double defense, double k) {
        if (defense <= 0) return 0.0;
        double reduction = defense / (defense + k); // 0..1
        return Math.min(20.0, 25.0 * reduction);
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
        String uuid = player.getStringUUID();
        ModNetworking.send(player, new StatSyncS2C(
                (float) s.defense, (float) s.strength,
                (float) s.maxMana, (float) getMana(player),
                (float) s.maxCursedEnergy, (float) getCursedEnergy(player),
                (float) s.critChance, (float) s.ferocity, (float) s.speed,
                DataManager.sorcererGrade(uuid),
                com.political.curse.rules.JjkRules.packHudFlags(player)));
    }

    public static void tickAll(MinecraftServer server) {
        tickCounter++;
        boolean secondTick = (tickCounter % 20 == 0);
        if (!secondTick) return;

        com.political.config.PoliticalConfig cfg = com.political.config.PoliticalConfig.get();
        double manaRate = DataManager.data().manaRegenRate * cfg.manaRegenMultiplier;
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
                double rate = 0.015 * s.cursedRegenMultiplier * (1.0 + grade * 0.12)
                        * cfg.cursedEnergyRegenMultiplier;
                double next = Math.min(s.maxCursedEnergy, cursed + s.maxCursedEnergy * rate);
                CURSED.put(player.getUUID(), next);
                DataManager.setStoredCursedEnergy(player.getStringUUID(), next);
            }

            // The bottom stat readout is now rendered client-side by the HUD overlay
            // (see PoliticalClient#renderHud), driven by StatSyncS2C, instead of the
            // server action bar. Court domains still drive their own action bar separately.
        }
    }

    public static void remove(UUID uuid) {
        Double cursed = CURSED.get(uuid);
        if (cursed != null) DataManager.setStoredCursedEnergy(uuid.toString(), cursed);
        STATS.remove(uuid);
        MANA.remove(uuid);
        CURSED.remove(uuid);
    }
}
