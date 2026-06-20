package com.political.economy;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/** Personal bank accounts with daily compounding interest. */
public final class BankManager {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final double DAILY_INTEREST = 0.02;

    private BankManager() {}

    public static int balance(String uuid) {
        return DataManager.data().bank.getOrDefault(uuid, 0);
    }

    public static boolean deposit(String uuid, int amount) {
        if (!DataManager.removeCoins(uuid, amount)) return false;
        DataManager.data().bank.merge(uuid, amount, Integer::sum);
        return true;
    }

    public static boolean withdraw(String uuid, int amount) {
        int bal = balance(uuid);
        if (bal < amount) return false;
        DataManager.data().bank.put(uuid, bal - amount);
        DataManager.addCoins(uuid, amount);
        return true;
    }

    public static void tickInterest(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        long now = System.currentTimeMillis();
        if (d.lastInterestTime != 0 && now - d.lastInterestTime < DAY_MS) return;
        d.lastInterestTime = now;
        for (var entry : d.bank.entrySet()) {
            int gain = (int) Math.floor(entry.getValue() * DAILY_INTEREST);
            if (gain > 0) entry.setValue(entry.getValue() + gain);
        }
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("Bank interest has been applied to all accounts.").withStyle(ChatFormatting.AQUA), false);
    }
}
