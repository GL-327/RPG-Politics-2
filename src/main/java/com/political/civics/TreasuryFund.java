package com.political.civics;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Random;

/**
 * Sovereign wealth fund + public works. The fund invests treasury coins into a
 * synthetic index that drifts via a random walk, so the state can grow (or lose)
 * its reserves over time. Completed public works grant server-wide buffs.
 */
public final class TreasuryFund {

    private static final long TICK_INTERVAL = 1200L; // ~1 minute price walk
    private static final int EFFECT_DURATION = 220;
    private static final Random RNG = new Random();
    private static long counter = 0;
    private static int worksTickCounter = 0;

    private TreasuryFund() {}

    public static double index() {
        return DataManager.data().treasuryFundIndex;
    }

    /** Current market value of fund holdings, in coins. */
    public static long fundValue() {
        PoliticsData d = DataManager.data();
        return (long) Math.floor(d.treasuryFundUnits * d.treasuryFundIndex / 1000.0);
    }

    /** Invests treasury coins into the fund at the current index. Returns false if treasury short. */
    public static boolean invest(int coins) {
        PoliticsData d = DataManager.data();
        if (coins <= 0 || !DataManager.removeTreasury(coins)) return false;
        double units = coins / (d.treasuryFundIndex / 1000.0);
        d.treasuryFundUnits += units;
        d.treasuryFund += coins;
        return true;
    }

    /** Liquidates coins of value from the fund back to the treasury. Returns coins realised. */
    public static long divest(int coins) {
        PoliticsData d = DataManager.data();
        long value = fundValue();
        if (value <= 0) return 0;
        int realise = (int) Math.min(coins, value);
        double units = realise / (d.treasuryFundIndex / 1000.0);
        d.treasuryFundUnits = Math.max(0, d.treasuryFundUnits - units);
        d.treasuryFund = Math.max(0, d.treasuryFund - realise);
        DataManager.addTreasury(realise);
        return realise;
    }

    // --- Public works ---

    public static int invested(PublicProject project) {
        return DataManager.data().publicWorks.getOrDefault(project.id, 0);
    }

    public static boolean isComplete(PublicProject project) {
        return DataManager.data().completedWorks.contains(project.id);
    }

    /** Channels treasury coins into a project; completes it once the cost is met. */
    public static int fundProject(MinecraftServer server, PublicProject project, int coins) {
        PoliticsData d = DataManager.data();
        if (isComplete(project)) return -1;
        int have = invested(project);
        int need = project.cost - have;
        int spend = Math.min(coins, need);
        if (spend <= 0 || !DataManager.removeTreasury(spend)) return 0;
        int total = have + spend;
        d.publicWorks.put(project.id, total);
        if (total >= project.cost) {
            d.completedWorks.add(project.id);
            d.publicWorks.remove(project.id);
            server.getPlayerList().broadcastSystemMessage(
                    Component.literal("Public works complete: ").withStyle(ChatFormatting.GOLD)
                            .copy().append(Component.literal(project.displayName).withStyle(project.color, ChatFormatting.BOLD)),
                    false);
        }
        return spend;
    }

    public static void tick(MinecraftServer server) {
        // Price walk for the wealth fund.
        if (++counter % TICK_INTERVAL == 0) {
            PoliticsData d = DataManager.data();
            double change = RNG.nextGaussian() * 0.04 + 0.001; // slight upward bias
            d.treasuryFundIndex = Math.max(100.0, d.treasuryFundIndex * (1.0 + change));
            d.lastFundTick = System.currentTimeMillis();
        }
        // Apply completed public works buffs.
        if (++worksTickCounter % 100 == 0) applyWorks(server);
    }

    private static void applyWorks(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        if (d.completedWorks.isEmpty()) return;
        boolean roads = d.completedWorks.contains(PublicProject.ROADS.id);
        boolean granary = d.completedWorks.contains(PublicProject.GRANARY.id);
        boolean aqueduct = d.completedWorks.contains(PublicProject.AQUEDUCT.id);
        boolean hospital = d.completedWorks.contains(PublicProject.HOSPITAL.id);
        boolean university = d.completedWorks.contains(PublicProject.UNIVERSITY.id);
        boolean walls = d.completedWorks.contains(PublicProject.WALLS.id);

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            // Benefits are for enrolled citizens of any settlement.
            if (DataManager.citizenshipOf(p.getStringUUID()) == null) continue;
            if (roads) p.addEffect(new MobEffectInstance(MobEffects.SPEED, EFFECT_DURATION, 0, true, false));
            if (granary) p.addEffect(new MobEffectInstance(MobEffects.SATURATION, EFFECT_DURATION, 0, true, false));
            if (aqueduct) p.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, EFFECT_DURATION, 0, true, false));
            if (hospital) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 0, true, false));
            if (university) p.addEffect(new MobEffectInstance(MobEffects.HASTE, EFFECT_DURATION, 0, true, false));
            if (walls) p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, EFFECT_DURATION, 0, true, false));
        }
    }

    public static String summary() {
        PoliticsData d = DataManager.data();
        StringBuilder sb = new StringBuilder();
        sb.append("Sovereign Wealth Fund:\n");
        sb.append("  Index: ").append(String.format("%.1f", d.treasuryFundIndex)).append('\n');
        sb.append("  Principal invested: ").append(d.treasuryFund).append(" coins\n");
        sb.append("  Market value: ").append(fundValue()).append(" coins\n");
        sb.append("Public works:\n");
        for (PublicProject project : PublicProject.values()) {
            if (isComplete(project)) {
                sb.append("  [DONE] ").append(project.displayName).append('\n');
            } else {
                sb.append("  ").append(project.displayName).append(": ")
                        .append(invested(project)).append('/').append(project.cost).append(" coins\n");
            }
        }
        return sb.toString();
    }
}
