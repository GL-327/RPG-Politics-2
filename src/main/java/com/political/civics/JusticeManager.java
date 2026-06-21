package com.political.civics;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import com.political.politics.PrisonManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Justice flow that complements {@link PrisonManager}: fines, criminal records,
 * a player-wanted bounty board, and bail to buy early release. Fines and bail
 * feed the treasury; wanted bounties pay the captor on a wanted player's death.
 */
public final class JusticeManager {

    private JusticeManager() {}

    // --- Fines & records ---

    public static int outstandingFine(String uuid) {
        return DataManager.data().fines.getOrDefault(uuid, 0);
    }

    public static int record(String uuid) {
        return DataManager.data().criminalRecord.getOrDefault(uuid, 0);
    }

    public static void fine(MinecraftServer server, String targetUuid, int amount) {
        PoliticsData d = DataManager.data();
        d.fines.merge(targetUuid, amount, Integer::sum);
        d.criminalRecord.merge(targetUuid, 1, Integer::sum);
        ServerPlayer online = playerByUuid(server, targetUuid);
        if (online != null) {
            online.sendSystemMessage(Component.literal("You have been fined " + amount + " coins. Pay with /fine pay.")
                    .withStyle(ChatFormatting.RED));
        }
    }

    /** Pays as much of the outstanding fine as the player can afford; remainder stays owed. */
    public static int payFine(ServerPlayer player) {
        String uuid = player.getStringUUID();
        int owed = outstandingFine(uuid);
        if (owed <= 0) return 0;
        int pay = Math.min(owed, DataManager.getCoins(uuid));
        if (pay <= 0) return 0;
        DataManager.removeCoins(uuid, pay);
        DataManager.addTreasury(pay);
        int left = owed - pay;
        if (left <= 0) DataManager.data().fines.remove(uuid);
        else DataManager.data().fines.put(uuid, left);
        return pay;
    }

    // --- Wanted board ---

    public static boolean isWanted(String uuid) {
        return DataManager.data().wanted.getOrDefault(uuid, 0) > 0;
    }

    public static int wantedReward(String uuid) {
        return DataManager.data().wanted.getOrDefault(uuid, 0);
    }

    public static void postWanted(MinecraftServer server, String targetUuid, int reward) {
        DataManager.data().wanted.merge(targetUuid, reward, Integer::sum);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("WANTED: " + DataManager.nameOf(targetUuid) + " — reward "
                        + wantedReward(targetUuid) + " coins, dead or captured.")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
    }

    public static int clearWanted(String uuid) {
        Integer r = DataManager.data().wanted.remove(uuid);
        return r == null ? 0 : r;
    }

    /** Death hook: pay the killer if the victim was a wanted player. */
    public static void onPlayerKilled(ServerPlayer victim, ServerPlayer killer) {
        if (killer == null || victim == null) return;
        String vId = victim.getStringUUID();
        int reward = wantedReward(vId);
        if (reward <= 0) return;
        clearWanted(vId);
        int fromTreasury = Math.min(reward, DataManager.getTreasury());
        if (fromTreasury > 0) DataManager.removeTreasury(fromTreasury);
        DataManager.addCoins(killer.getStringUUID(), reward);
        JobManager.onCapture(killer);
        killer.sendSystemMessage(Component.literal("Bounty claimed on " + victim.getName().getString()
                + ": +" + reward + " coins.").withStyle(ChatFormatting.GREEN));
        victim.sendSystemMessage(Component.literal("Your bounty was claimed by " + killer.getName().getString() + ".")
                .withStyle(ChatFormatting.DARK_RED));
    }

    // --- Bail ---

    public static int bailOf(String uuid) {
        return DataManager.data().bail.getOrDefault(uuid, 0);
    }

    public static void setBail(String uuid, int amount) {
        if (amount <= 0) DataManager.data().bail.remove(uuid);
        else DataManager.data().bail.put(uuid, amount);
    }

    /** Prisoner pays their set bail to win immediate release. */
    public static boolean payBail(ServerPlayer player) {
        String uuid = player.getStringUUID();
        if (!PrisonManager.isPrisoner(uuid)) return false;
        int amount = bailOf(uuid);
        if (amount <= 0) return false;
        if (!DataManager.removeCoins(uuid, amount)) return false;
        DataManager.addTreasury(amount);
        DataManager.data().bail.remove(uuid);
        PrisonManager.release(player);
        return true;
    }

    private static ServerPlayer playerByUuid(MinecraftServer server, String uuid) {
        try {
            return server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
