package com.political.combat;

import com.political.court.CourtDomainManager;
import com.political.net.ModNetworking;
import com.political.net.StatSyncS2C;
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
 * plus a synced mana value feed the client HUD.
 *
 * <p>Per-item bonuses are read from {@code custom_data} integer keys
 * {@code rpg_health}, {@code rpg_defense}, {@code rpg_strength}, {@code rpg_intelligence}.
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

    private static final EquipmentSlot[] SCANNED_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    private static final ConcurrentHashMap<UUID, RpgStats> STATS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Double> MANA = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

    private StatManager() {}

    public static RpgStats get(ServerPlayer player) {
        return STATS.computeIfAbsent(player.getUUID(), u -> compute(player));
    }

    public static double getMana(ServerPlayer player) {
        return MANA.getOrDefault(player.getUUID(), BASE_MAX_MANA);
    }

    public static boolean spendMana(ServerPlayer player, double cost) {
        double current = getMana(player);
        if (current < cost) return false;
        MANA.put(player.getUUID(), current - cost);
        return true;
    }

    public static RpgStats compute(ServerPlayer player) {
        RpgStats s = new RpgStats();
        for (EquipmentSlot slot : SCANNED_SLOTS) {
            addItemStats(s, player.getItemBySlot(slot));
        }
        return s;
    }

    private static void addItemStats(RpgStats s, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        var tag = data.copyTag();
        s.maxHealth += tag.getIntOr("rpg_health", 0);
        s.defense += tag.getIntOr("rpg_defense", 0);
        s.strength += tag.getIntOr("rpg_strength", 0);
        s.maxMana += tag.getIntOr("rpg_intelligence", 0);
    }

    public static void apply(ServerPlayer player) {
        RpgStats s = compute(player);
        STATS.put(player.getUUID(), s);

        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), RPG_HEALTH_ID, s.maxHealth - VANILLA_BASE_HEALTH);
        // Defense maps onto vanilla armor (capped at 30 by vanilla); strength onto attack damage.
        applyModifier(player.getAttribute(Attributes.ARMOR), RPG_DEFENSE_ID, s.defense * 0.15);
        applyModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), RPG_STRENGTH_ID, s.strength * 0.1);

        double mana = Math.min(MANA.getOrDefault(player.getUUID(), s.maxMana), s.maxMana);
        MANA.put(player.getUUID(), mana);
        sync(player);
    }

    private static void applyModifier(AttributeInstance instance, Identifier id, double amount) {
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void sync(ServerPlayer player) {
        RpgStats s = get(player);
        ModNetworking.send(player, new StatSyncS2C(
                (float) s.defense, (float) s.strength, (float) s.maxMana, (float) getMana(player)));
    }

    public static void tickAll(MinecraftServer server) {
        tickCounter++;
        boolean secondTick = (tickCounter % 20 == 0);
        if (!secondTick) return;

        boolean courtActive = CourtDomainManager.isActive();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            apply(player);

            RpgStats s = get(player);
            double mana = getMana(player);
            if (mana < s.maxMana) {
                MANA.put(player.getUUID(), Math.min(s.maxMana, mana + s.maxMana * 0.02));
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
        Component bar = Component.literal("\u2764 " + hp + "/" + maxHp + "    ").withStyle(ChatFormatting.RED)
                .append(Component.literal("\u2748 " + (int) s.defense + " Def    ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal("\u2726 " + (int) s.strength + " Str    ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("\u270E " + mana + "/" + (int) s.maxMana + " Mana").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(bar, true);
    }

    public static void remove(UUID uuid) {
        STATS.remove(uuid);
        MANA.remove(uuid);
    }
}
