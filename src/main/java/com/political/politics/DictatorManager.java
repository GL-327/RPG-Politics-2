package com.political.politics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;

/** Dictatorship: suspends elections, absorbs judiciary powers, smite + summon abilities. */
public final class DictatorManager {

    private static final long SMITE_COOLDOWN_MS = 15_000L;
    private static long lastSmite = 0L;

    private DictatorManager() {}

    public static void setDictator(ServerPlayer player, MinecraftServer server) {
        PoliticsData d = DataManager.data();
        d.previousJudge = d.judge;
        d.judge = "";
        d.dictatorActive = true;
        d.dictator = player.getStringUUID();
        server.getPlayerList().broadcastSystemMessage(
                Component.literal(player.getName().getString() + " has seized power as Dictator. Elections are suspended.")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
    }

    public static void removeDictator(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        d.dictatorActive = false;
        d.dictator = "";
        d.judge = d.previousJudge;
        d.previousJudge = "";
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("The dictatorship has ended. Order is restored.")
                        .withStyle(ChatFormatting.GREEN), false);
    }

    public static boolean canSmite() {
        return System.currentTimeMillis() - lastSmite >= SMITE_COOLDOWN_MS;
    }

    public static void smite(ServerPlayer target) {
        lastSmite = System.currentTimeMillis();
        ServerLevel level = target.level();
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(target.getX(), target.getY(), target.getZ());
            level.addFreshEntity(bolt);
        }
    }

    public static void summon(ServerPlayer caller, ServerPlayer target) {
        target.connection.teleport(caller.getX(), caller.getY(), caller.getZ(), target.getYRot(), target.getXRot());
        target.sendSystemMessage(Component.literal("You have been summoned by the authorities.")
                .withStyle(ChatFormatting.RED));
    }
}
