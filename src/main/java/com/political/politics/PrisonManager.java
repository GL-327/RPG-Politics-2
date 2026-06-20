package com.political.politics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

/** Imprisonment, release, and exile. Prisoners are locked to Adventure mode and a cell location. */
public final class PrisonManager {

    private static final double PRISON_Y = 100.0;

    private PrisonManager() {}

    public static boolean isPrisoner(String uuid) {
        Long until = DataManager.data().prisoners.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    public static void imprison(ServerPlayer target, int minutes, double x, double z) {
        String uuid = target.getStringUUID();
        long release = System.currentTimeMillis() + (long) minutes * 60_000L;
        DataManager.data().prisoners.put(uuid, release);
        DataManager.data().prisonerLocations.put(uuid, x + "," + PRISON_Y + "," + z);
        target.connection.teleport(x, PRISON_Y, z, target.getYRot(), target.getXRot());
        target.setGameMode(GameType.ADVENTURE);
        target.sendSystemMessage(Component.literal("You have been imprisoned for " + minutes + " minute(s).")
                .withStyle(ChatFormatting.RED));
    }

    public static void release(ServerPlayer player) {
        String uuid = player.getStringUUID();
        DataManager.data().prisoners.remove(uuid);
        DataManager.data().prisonerLocations.remove(uuid);
        player.setGameMode(GameType.SURVIVAL);
        player.sendSystemMessage(Component.literal("You have been released.").withStyle(ChatFormatting.GREEN));
    }

    /** OP/Judge pardon by UUID, even when offline. */
    public static void pardon(String uuid, MinecraftServer server) {
        DataManager.data().prisoners.remove(uuid);
        DataManager.data().prisonerLocations.remove(uuid);
        ServerPlayer online = server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
        if (online != null) {
            online.setGameMode(GameType.SURVIVAL);
            online.sendSystemMessage(Component.literal("You have been pardoned.").withStyle(ChatFormatting.GREEN));
        }
    }

    public static void exile(ServerPlayer target) {
        double angle = Math.random() * Math.PI * 2;
        double dist = 20_000 + Math.random() * 80_000;
        double x = Math.cos(angle) * dist;
        double z = Math.sin(angle) * dist;
        target.connection.teleport(x, 200, z, target.getYRot(), target.getXRot());
        target.sendSystemMessage(Component.literal("You have been exiled to the frontier.").withStyle(ChatFormatting.DARK_RED));
    }

    public static void checkPlayerJoin(ServerPlayer player) {
        String uuid = player.getStringUUID();
        if (isPrisoner(uuid)) {
            String loc = DataManager.data().prisonerLocations.get(uuid);
            if (loc != null) {
                String[] parts = loc.split(",");
                try {
                    player.connection.teleport(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]), player.getYRot(), player.getXRot());
                } catch (NumberFormatException ignored) { }
            }
            player.setGameMode(GameType.ADVENTURE);
        } else if (DataManager.data().prisoners.containsKey(uuid)) {
            release(player);
        }
    }

    public static void tick(MinecraftServer server) {
        if (DataManager.data().prisoners.isEmpty()) return;
        long now = System.currentTimeMillis();
        List<String> expired = new ArrayList<>();
        for (var entry : DataManager.data().prisoners.entrySet()) {
            if (now >= entry.getValue()) expired.add(entry.getKey());
        }
        for (String uuid : expired) {
            ServerPlayer online = server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
            if (online != null) {
                release(online);
            } else {
                DataManager.data().prisoners.remove(uuid);
                DataManager.data().prisonerLocations.remove(uuid);
            }
        }
    }
}
