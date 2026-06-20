package com.political.combat;

import com.political.CoinManager;
import com.political.net.ModNetworking;
import com.political.net.StatSyncS2C;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Computes and applies RPG stats. This is the foundation that overrides vanilla
 * health (base raised to {@link #BASE_MAX_HEALTH}) and feeds the custom HUD.
 *
 * <p>Stat bonuses are read from equipped items' {@code custom_data} NBT keys
 * ({@code rpg_health}, {@code rpg_defense}, {@code rpg_strength},
 * {@code rpg_crit_chance}, {@code rpg_crit_damage}, {@code rpg_intelligence}),
 * which integrates cleanly with the existing NBT-based custom item system.
 */
public final class StatManager {

    public static final double BASE_MAX_HEALTH = 100.0;
    public static final double BASE_DEFENSE = 0.0;
    public static final double BASE_STRENGTH = 0.0;
    public static final double BASE_CRIT_CHANCE = 30.0;
    public static final double BASE_CRIT_DAMAGE = 50.0;
    public static final double BASE_MAX_MANA = 100.0;

    /** Vanilla players have 20 max health by default; we add the rest as a modifier. */
    private static final double VANILLA_BASE_HEALTH = 20.0;
    private static final Identifier RPG_HEALTH_ID = Identifier.of("politicalserver", "rpg_base_health");

    private static final EquipmentSlot[] SCANNED_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    private static final ConcurrentHashMap<UUID, RpgStats> STATS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Double> MANA = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

    private StatManager() {}

    public static RpgStats get(ServerPlayerEntity player) {
        return STATS.computeIfAbsent(player.getUuid(), u -> compute(player));
    }

    public static double getMana(ServerPlayerEntity player) {
        return MANA.getOrDefault(player.getUuid(), BASE_MAX_MANA);
    }

    /** Attempts to spend mana; returns true and deducts it if the player can afford it. */
    public static boolean spendMana(ServerPlayerEntity player, double cost) {
        double current = getMana(player);
        if (current < cost) return false;
        MANA.put(player.getUuid(), current - cost);
        sync(player);
        return true;
    }

    public static RpgStats compute(ServerPlayerEntity player) {
        RpgStats s = new RpgStats();
        for (EquipmentSlot slot : SCANNED_SLOTS) {
            addItemStats(s, player.getEquippedStack(slot));
        }
        return s;
    }

    private static void addItemStats(RpgStats s, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return;
        NbtCompound nbt = data.copyNbt();
        s.maxHealth += nbt.getInt("rpg_health", 0);
        s.defense += nbt.getInt("rpg_defense", 0);
        s.strength += nbt.getInt("rpg_strength", 0);
        s.critChance += nbt.getInt("rpg_crit_chance", 0);
        s.critDamage += nbt.getInt("rpg_crit_damage", 0);
        s.maxMana += nbt.getInt("rpg_intelligence", 0);
    }

    /** Recomputes the player's stats, applies the health attribute, and syncs the HUD. */
    public static void apply(ServerPlayerEntity player) {
        RpgStats s = compute(player);
        STATS.put(player.getUuid(), s);

        EntityAttributeInstance hp = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (hp != null) {
            hp.removeModifier(RPG_HEALTH_ID);
            double add = s.maxHealth - VANILLA_BASE_HEALTH;
            if (add != 0) {
                hp.addPersistentModifier(new EntityAttributeModifier(
                        RPG_HEALTH_ID, add, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }

        // Clamp mana to the new max, initialising to full for new players.
        double mana = Math.min(MANA.getOrDefault(player.getUuid(), s.maxMana), s.maxMana);
        MANA.put(player.getUuid(), mana);
        sync(player);
    }

    public static void sync(ServerPlayerEntity player) {
        RpgStats s = get(player);
        ModNetworking.send(player, new StatSyncS2C(
                (float) s.defense,
                (float) s.strength,
                (float) s.maxMana,
                (float) getMana(player),
                CoinManager.getCoins(player)));
    }

    /** Called every server tick: regenerates mana and periodically recomputes gear stats. */
    public static void tickAll(MinecraftServer server) {
        tickCounter++;
        boolean recompute = (tickCounter % 20 == 0);
        boolean regen = (tickCounter % 20 == 0);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (recompute) {
                apply(player);
            }
            if (regen) {
                RpgStats s = get(player);
                double mana = getMana(player);
                if (mana < s.maxMana) {
                    MANA.put(player.getUuid(), Math.min(s.maxMana, mana + s.maxMana * 0.02));
                    if (!recompute) sync(player);
                }
            }
        }
    }

    public static void remove(UUID uuid) {
        STATS.remove(uuid);
        MANA.remove(uuid);
    }
}
