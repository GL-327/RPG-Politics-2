package com.political.economy;

import com.political.net.BankMenuS2C;
import com.political.net.ModNetworking;
import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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

    // ---------------- Bank GUI ----------------

    /** Builds and sends a fresh Bank menu snapshot to the player's client. */
    public static void sendMenu(ServerPlayer player) {
        String uuid = player.getStringUUID();
        ModNetworking.send(player, new BankMenuS2C(
                DataManager.getCoins(uuid), balance(uuid), DataManager.getCredits(uuid),
                DataManager.netWorth(uuid), (int) Math.round(DAILY_INTEREST * 1000)));
    }

    /** Handles a {@link com.political.net.BankActionC2S} from the GUI, then resends the menu. */
    public static void handleAction(ServerPlayer player, String action, int amount) {
        String uuid = player.getStringUUID();
        switch (action) {
            case "deposit" -> {
                if (amount > 0 && !deposit(uuid, amount)) notify(player, "You don't have that many coins to deposit.");
            }
            case "withdraw" -> {
                if (amount > 0 && !withdraw(uuid, amount)) notify(player, "Your bank balance is too low.");
            }
            case "depositAll" -> {
                int w = DataManager.getCoins(uuid);
                if (w > 0) deposit(uuid, w);
            }
            case "withdrawAll" -> {
                int b = balance(uuid);
                if (b > 0) withdraw(uuid, b);
            }
            default -> { }
        }
        sendMenu(player);
    }

    private static void notify(ServerPlayer player, String msg) {
        player.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED), true);
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
