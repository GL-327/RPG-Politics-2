package com.political.civics;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Profession & daily income system. Players pick a {@link Job}; online players
 * receive a daily wage (drawn from the treasury when solvent), and may call
 * {@code /job work} on a cooldown for an active payout that also builds job XP.
 */
public final class JobManager {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final long WORK_COOLDOWN_MS = 5L * 60 * 1000; // 5 minutes
    private static final int MAX_LEVEL = 25;
    private static int tickCounter = 0;

    private JobManager() {}

    public static Job jobOf(String uuid) {
        return Job.byId(DataManager.data().civicJob.get(uuid));
    }

    public static boolean setJob(ServerPlayer player, Job job) {
        DataManager.data().civicJob.put(player.getStringUUID(), job.name());
        return true;
    }

    public static boolean quitJob(String uuid) {
        return DataManager.data().civicJob.remove(uuid) != null;
    }

    public static int xp(String uuid) {
        return DataManager.data().civicJobXp.getOrDefault(uuid, 0);
    }

    public static int level(String uuid) {
        return Math.min(MAX_LEVEL, (int) Math.floor(Math.sqrt(xp(uuid) / 200.0)));
    }

    public static int xpForNext(String uuid) {
        int next = level(uuid) + 1;
        return (int) (next * next * 200.0);
    }

    public static void addXp(String uuid, int amount) {
        if (amount <= 0) return;
        DataManager.data().civicJobXp.merge(uuid, amount, Integer::sum);
    }

    /** Wage scales with job level; banker pays more but is taxed harder elsewhere. */
    public static int dailyWage(String uuid) {
        Job job = jobOf(uuid);
        if (job == null) return 0;
        double mult = 1.0 + level(uuid) * 0.08;
        double law = LawManager.isActive(CivicLaw.STIMULUS) ? 1.25 : 1.0;
        return (int) Math.round(job.baseWage * mult * law);
    }

    /** Pays a wage, preferring treasury funds; small top-up is minted if treasury is dry. */
    private static void payWage(ServerPlayer player, int wage) {
        String uuid = player.getStringUUID();
        int fromTreasury = Math.min(wage, DataManager.getTreasury());
        if (fromTreasury > 0) DataManager.removeTreasury(fromTreasury);
        int minted = wage - fromTreasury;
        DataManager.addCoins(uuid, wage);
        addXp(uuid, 10);
        Job job = jobOf(uuid);
        player.sendSystemMessage(Component.literal("Daily wage as " + (job == null ? "worker" : job.displayName)
                + ": +" + wage + " coins" + (minted > 0 ? " (treasury short by " + minted + ")" : "") + ".")
                .withStyle(ChatFormatting.GOLD));
    }

    /** Active work payout with cooldown; returns seconds remaining if still on cooldown, else -1. */
    public static long work(ServerPlayer player) {
        String uuid = player.getStringUUID();
        Job job = jobOf(uuid);
        if (job == null) return -2; // no job
        PoliticsData d = DataManager.data();
        long now = System.currentTimeMillis();
        long last = d.civicJobLastWork.getOrDefault(uuid, 0L);
        long elapsed = now - last;
        if (last != 0 && elapsed < WORK_COOLDOWN_MS) {
            return (WORK_COOLDOWN_MS - elapsed) / 1000L;
        }
        d.civicJobLastWork.put(uuid, now);
        int pay = Math.max(8, job.baseWage / 8 + level(uuid) * 2);
        int fromTreasury = Math.min(pay, DataManager.getTreasury());
        if (fromTreasury > 0) DataManager.removeTreasury(fromTreasury);
        DataManager.addCoins(uuid, pay);
        addXp(uuid, 25);
        player.sendSystemMessage(Component.literal(workFlavor(job) + " +" + pay + " coins, +25 job XP.")
                .withStyle(ChatFormatting.GREEN));
        return -1;
    }

    private static String workFlavor(Job job) {
        return switch (job) {
            case MINER -> "You haul a cartload of ore to the surface.";
            case FARMER -> "You bring in a fresh harvest.";
            case HUNTER -> "You return from a successful hunt.";
            case GUARD -> "You complete a patrol of the streets.";
            case MERCHANT -> "You close a brisk round of trades.";
            case BANKER -> "You balance the day's ledgers.";
            case BUILDER -> "You finish a hard day on the scaffolds.";
            case DIPLOMAT -> "You broker a delicate agreement.";
        };
    }

    /** Bonus job XP routed from bounty kills (hunter) — wired via the death hook. */
    public static void onBountyKill(ServerPlayer killer) {
        if (jobOf(killer.getStringUUID()) == Job.HUNTER) addXp(killer.getStringUUID(), 15);
    }

    /** Bonus job XP for guards who capture wanted criminals. */
    public static void onCapture(ServerPlayer captor) {
        if (jobOf(captor.getStringUUID()) == Job.GUARD) addXp(captor.getStringUUID(), 30);
    }

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 200 != 0) return; // ~every 10s
        long now = System.currentTimeMillis();
        PoliticsData d = DataManager.data();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getStringUUID();
            if (jobOf(uuid) == null) continue;
            long last = d.civicJobLastWage.getOrDefault(uuid, 0L);
            if (last == 0L) { d.civicJobLastWage.put(uuid, now); continue; }
            if (now - last >= DAY_MS) {
                d.civicJobLastWage.put(uuid, now);
                payWage(player, dailyWage(uuid));
            }
        }
    }
}
