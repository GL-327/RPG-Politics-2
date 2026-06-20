package com.political.player;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;

import java.util.Set;

/** Serialises and restores player positions (with dimension) for homes, checkpoints, spawn. */
public final class Locations {

    private Locations() {}

    /** "x,y,z,yaw,pitch,dimension" */
    public static String serialize(ServerPlayer player) {
        return player.getX() + "," + player.getY() + "," + player.getZ() + ","
                + player.getYRot() + "," + player.getXRot() + ","
                + player.level().dimension().identifier();
    }

    /** Teleports the player to a serialized location. Returns false if it could not be parsed. */
    public static boolean teleport(ServerPlayer player, String serialized) {
        if (serialized == null || serialized.isEmpty()) return false;
        String[] p = serialized.split(",");
        if (p.length < 5) return false;
        try {
            double x = Double.parseDouble(p[0]);
            double y = Double.parseDouble(p[1]);
            double z = Double.parseDouble(p[2]);
            float yaw = Float.parseFloat(p[3]);
            float pitch = Float.parseFloat(p[4]);
            ServerLevel level = player.level();
            if (p.length >= 6) {
                ServerLevel resolved = resolve(player.level().getServer(), p[5]);
                if (resolved != null) level = resolved;
            }
            player.teleportTo(level, x, y, z, Set.<Relative>of(), yaw, pitch, true);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static ServerLevel resolve(MinecraftServer server, String dimension) {
        if (server == null) return null;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension));
        return server.getLevel(key);
    }
}
