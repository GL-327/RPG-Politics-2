package com.political.bounty;

import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Bounty Hunt progression, quest tracking, and scaled-boss spawning/reward loop. */
public final class BountyManager {

    public record ActiveBoss(UUID owner, BountyType type, int tier) {}

    private static final Map<UUID, ActiveBoss> ACTIVE_BOSSES = new HashMap<>();
    private static final Map<UUID, Boolean> ENRAGED = new HashMap<>();
    private static int tickCounter = 0;

    private BountyManager() {}

    // ---------------- Quests ----------------

    /** Quest payload serialised as "TYPE|tier|killsDone|killsNeeded|bossSpawned". */
    private static int killsNeeded(int tier) {
        return 8 + tier * 4;
    }

    public static boolean startQuest(ServerPlayer player, BountyType type, int tier) {
        String uuid = player.getStringUUID();
        if (DataManager.data().activeQuests.containsKey(uuid)) return false;
        int need = killsNeeded(tier);
        DataManager.data().activeQuests.put(uuid, type.name() + "|" + tier + "|0|" + need + "|0");
        player.sendSystemMessage(Component.literal("Quest accepted: slay " + need + " " + type.displayName
                + " creatures to summon the boss.").withStyle(ChatFormatting.GOLD));
        return true;
    }

    public static boolean cancelQuest(ServerPlayer player) {
        return DataManager.data().activeQuests.remove(player.getStringUUID()) != null;
    }

    public static String questStatus(String uuid) {
        String q = DataManager.data().activeQuests.get(uuid);
        if (q == null) return "No active quest.";
        String[] p = q.split("\\|");
        BountyType t = BountyType.byId(p[0]);
        return "Quest: " + (t == null ? p[0] : t.displayName) + " T" + p[1] + " - " + p[2] + "/" + p[3]
                + (p[4].equals("1") ? " (boss summoned!)" : "");
    }

    private static void onQuestKill(ServerPlayer killer, LivingEntity victim) {
        String uuid = killer.getStringUUID();
        String q = DataManager.data().activeQuests.get(uuid);
        if (q == null) return;
        String[] p = q.split("\\|");
        BountyType type = BountyType.byId(p[0]);
        if (type == null) return;
        if (victim.getType() != type.entityType) return;
        if (p[4].equals("1")) return; // boss already summoned

        int tier = Integer.parseInt(p[1]);
        int done = Integer.parseInt(p[2]) + 1;
        int need = Integer.parseInt(p[3]);
        if (done >= need) {
            DataManager.data().activeQuests.put(uuid, type.name() + "|" + tier + "|" + done + "|" + need + "|1");
            killer.sendSystemMessage(Component.literal("Quest complete! Summoning the " + type.displayName + " boss...")
                    .withStyle(ChatFormatting.GOLD));
            startBounty(killer, type, tier);
        } else {
            DataManager.data().activeQuests.put(uuid, type.name() + "|" + tier + "|" + done + "|" + need + "|0");
            if (done % 4 == 0) {
                killer.sendSystemMessage(Component.literal(type.displayName + " progress: " + done + "/" + need)
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    private static String key(String uuid, BountyType type) {
        return uuid + "|" + type.name();
    }

    public static int getXp(String uuid, BountyType type) {
        return DataManager.data().bountyXp.getOrDefault(key(uuid, type), 0);
    }

    public static int getLevel(String uuid, BountyType type) {
        return xpToLevel(getXp(uuid, type));
    }

    public static int xpToLevel(int xp) {
        return Math.min(50, (int) Math.floor(Math.sqrt(xp / 100.0)));
    }

    public static void addXp(String uuid, BountyType type, int amount) {
        DataManager.data().bountyXp.merge(key(uuid, type), amount, Integer::sum);
    }

    /** Spawns a scaled boss of the given discipline/tier near the player. */
    public static boolean startBounty(ServerPlayer player, BountyType type, int tier) {
        ServerLevel level = player.level();
        Mob boss = type.entityType.create(level, EntitySpawnReason.TRIGGERED);
        if (boss == null) return false;

        double maxHealth = 80.0 * tier;
        var healthAttr = boss.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.setBaseValue(maxHealth);
        var dmgAttr = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) dmgAttr.setBaseValue(4.0 * tier);
        boss.setHealth((float) maxHealth);

        boss.setCustomName(Component.literal(type.displayName + " Boss (T" + tier + ")")
                .withStyle(ChatFormatting.DARK_RED));
        boss.setCustomNameVisible(true);
        com.political.combat.HealthScalingManager.exempt(boss);

        boss.setPos(player.getX() + 3, player.getY(), player.getZ() + 3);
        level.addFreshEntity(boss);

        ACTIVE_BOSSES.put(boss.getUUID(), new ActiveBoss(player.getUUID(), type, tier));
        player.sendSystemMessage(Component.literal("A " + type.displayName + " boss (Tier " + tier + ") has spawned!")
                .withStyle(ChatFormatting.GOLD));
        return true;
    }

    /** Called from the death event; rewards the killer if the victim was a tracked boss. */
    public static void onEntityDeath(LivingEntity victim, ServerPlayer killer) {
        if (killer == null) return;
        ENRAGED.remove(victim.getUUID());
        ActiveBoss boss = ACTIVE_BOSSES.remove(victim.getUUID());
        if (boss == null) {
            onQuestKill(killer, victim);
            return;
        }
        // Boss defeated: clear any matching quest.
        String q = DataManager.data().activeQuests.get(killer.getStringUUID());
        if (q != null && q.startsWith(boss.type().name() + "|")) {
            DataManager.data().activeQuests.remove(killer.getStringUUID());
        }

        double xpMult = com.political.player.PartyManager.xpMultiplier(killer.getUUID());
        if (com.political.combat.AbilityEngine.hasEquipped(killer, com.political.items.Ability.XP_BOOST)) xpMult += 0.25;
        double coinMult = 1.0;
        if (com.political.combat.AbilityEngine.hasEquipped(killer, com.political.items.Ability.COIN_BOOST)) coinMult += 0.25;

        int xp = (int) Math.round(60 * boss.tier() * xpMult);
        int coins = (int) Math.round(150 * boss.tier() * coinMult);
        addXp(killer.getStringUUID(), boss.type(), xp);
        DataManager.addCoins(killer.getStringUUID(), coins);
        killer.sendSystemMessage(Component.literal("Bounty cleared! +" + xp + " " + boss.type().displayName
                + " XP, +" + coins + " coins. (Level " + getLevel(killer.getStringUUID(), boss.type()) + ")")
                .withStyle(ChatFormatting.GREEN));
    }

    /** Periodic boss behaviour: bosses enrage (strength + speed) once below 30% health. */
    public static void tick(MinecraftServer server) {
        if (++tickCounter % 20 != 0 || ACTIVE_BOSSES.isEmpty()) return;
        for (Map.Entry<UUID, ActiveBoss> e : ACTIVE_BOSSES.entrySet()) {
            UUID bossId = e.getKey();
            if (Boolean.TRUE.equals(ENRAGED.get(bossId))) continue;
            for (ServerLevel level : server.getAllLevels()) {
                Entity ent = level.getEntity(bossId);
                if (ent instanceof LivingEntity boss && boss.isAlive()) {
                    if (boss.getHealth() < boss.getMaxHealth() * 0.30f) {
                        boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 99999, 1, false, true));
                        boss.addEffect(new MobEffectInstance(MobEffects.SPEED, 99999, 1, false, true));
                        boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, true));
                        ENRAGED.put(bossId, true);
                    }
                    break;
                }
            }
        }
    }
}
