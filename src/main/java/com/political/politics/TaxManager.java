package com.political.politics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Daily percentage tax on coin balances, feeding the government treasury. */
public final class TaxManager {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private TaxManager() {}

    public static void tick(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        if (!d.taxEnabled) return;
        long now = System.currentTimeMillis();
        if (d.lastTaxTime != 0 && now - d.lastTaxTime < DAY_MS) return;
        d.lastTaxTime = now;
        collect(server);
    }

    public static void collect(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        int collected = 0;
        for (String uuid : new java.util.ArrayList<>(d.playerNames.keySet())) {
            if (uuid.equals(d.chair) || uuid.equals(d.viceChair)
                    || (d.dictatorActive && uuid.equals(d.dictator))) continue;
            int coins = DataManager.getCoins(uuid);
            int tax = (int) Math.ceil(coins * (d.taxPercent / 100.0));
            if (tax <= 0) continue;
            if (DataManager.removeCoins(uuid, tax)) {
                DataManager.addTreasury(tax);
                collected += tax;
            } else {
                d.taxOwed.merge(uuid, tax, Integer::sum);
            }
        }
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("Daily tax collected: " + collected + " coins to the treasury.")
                        .withStyle(ChatFormatting.GOLD), false);
    }

    public static boolean payTax(ServerPlayer player, int amount) {
        PoliticsData d = DataManager.data();
        String uuid = player.getStringUUID();
        int owed = d.taxOwed.getOrDefault(uuid, 0);
        if (owed <= 0) return false;
        int pay = Math.min(amount, owed);
        if (!DataManager.removeCoins(uuid, pay)) return false;
        DataManager.addTreasury(pay);
        int left = owed - pay;
        if (left <= 0) d.taxOwed.remove(uuid);
        else d.taxOwed.put(uuid, left);
        return true;
    }

    public static int taxOwed(String uuid) {
        return DataManager.data().taxOwed.getOrDefault(uuid, 0);
    }
}
