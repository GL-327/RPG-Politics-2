package com.political.civics;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Enacts, repeals, and applies the ongoing effects of {@link CivicLaw} decrees. */
public final class LawManager {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final int EFFECT_DURATION = 220; // ticks; refreshed each cycle
    private static int tickCounter = 0;

    private LawManager() {}

    public static boolean isActive(CivicLaw law) {
        return DataManager.data().activeLaws.contains(law.name());
    }

    /** Enacts a law, spending the treasury cost. Returns false if already active or treasury short. */
    public static boolean enact(MinecraftServer server, CivicLaw law) {
        PoliticsData d = DataManager.data();
        if (d.activeLaws.contains(law.name())) return false;
        if (!DataManager.removeTreasury(law.cost)) return false;
        d.activeLaws.add(law.name());
        d.lawEnactedAt.put(law.name(), System.currentTimeMillis());
        broadcast(server, Component.literal("A new law has been enacted: ")
                .withStyle(ChatFormatting.GOLD)
                .copy().append(Component.literal(law.displayName).withStyle(law.color, ChatFormatting.BOLD)));
        return true;
    }

    public static boolean repeal(MinecraftServer server, CivicLaw law) {
        PoliticsData d = DataManager.data();
        if (!d.activeLaws.remove(law.name())) return false;
        d.lawEnactedAt.remove(law.name());
        broadcast(server, Component.literal("Law repealed: " + law.displayName).withStyle(ChatFormatting.GRAY));
        return true;
    }

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 100 != 0) return; // every ~5s
        if (DataManager.data().activeLaws.isEmpty()) return;

        boolean stimulus = isActive(CivicLaw.STIMULUS);
        boolean healthcare = isActive(CivicLaw.PUBLIC_HEALTHCARE);
        boolean martial = isActive(CivicLaw.MARTIAL_LAW);
        boolean conscription = isActive(CivicLaw.CONSCRIPTION);
        int healthcareDrain = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getStringUUID();
            if (stimulus && JobManager.jobOf(uuid) != null) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, EFFECT_DURATION, 0, true, false));
            }
            if (healthcare) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 0, true, false));
                healthcareDrain += 2;
            }
            if (conscription) {
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, EFFECT_DURATION, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_DURATION, 0, true, false));
            }
            if (martial) {
                boolean enforcer = OfficeManager.holdsAny(uuid) || JobManager.jobOf(uuid) == Job.GUARD
                        || DataManager.isJudge(player.getUUID()) || DataManager.isChair(player.getUUID());
                boolean night = isNight(player.level());
                if (enforcer && night) {
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, EFFECT_DURATION, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_DURATION + 20, 0, true, false));
                }
                if (JusticeManager.isWanted(uuid)) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_DURATION, 0, true, false));
                }
            }
        }
        if (healthcareDrain > 0) DataManager.removeTreasury(healthcareDrain);

        if (isActive(CivicLaw.WELFARE)) tickWelfare(server);
    }

    private static void tickWelfare(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        long now = System.currentTimeMillis();
        if (d.lastWelfareTime != 0 && now - d.lastWelfareTime < DAY_MS) return;
        d.lastWelfareTime = now;
        int stipend = 200;
        int paid = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getStringUUID();
            if (DataManager.netWorth(uuid) >= 1000) continue;
            if (!DataManager.removeTreasury(stipend)) break;
            DataManager.addCoins(uuid, stipend);
            paid++;
            player.sendSystemMessage(Component.literal("Welfare stipend received: +" + stipend + " coins.")
                    .withStyle(ChatFormatting.AQUA));
        }
        if (paid > 0) {
            broadcast(server, Component.literal("Welfare paid to " + paid + " citizen(s).")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static boolean isNight(ServerLevel level) {
        return !level.isBrightOutside();
    }

    public static String summary() {
        PoliticsData d = DataManager.data();
        if (d.activeLaws.isEmpty()) return "No laws in force.";
        StringBuilder sb = new StringBuilder();
        for (String s : d.activeLaws) {
            CivicLaw law = CivicLaw.byId(s);
            if (law == null) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(law.displayName);
        }
        return sb.toString();
    }

    private static void broadcast(MinecraftServer server, Component msg) {
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }
}
