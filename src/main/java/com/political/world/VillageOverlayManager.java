package com.political.world;

import com.political.content.ModBlocks;
import com.political.politics.DataManager;
import com.political.politics.Settlement;
import com.political.politics.SettlementType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Registers {@link Settlement} records for vanilla / datapack villages when players
 * discover them, wiring the government layer onto natural village generation.
 */
public final class VillageOverlayManager {

    private static final double MIN_VILLAGER_CLUSTER = 2;
    private static final double SCAN_RADIUS = 48.0;
    private static final double MIN_SETTLEMENT_SEP = 80.0;
    private static final Set<Long> REGISTERED_CLUSTERS = new HashSet<>();

    private VillageOverlayManager() {}

    public static void tick(ServerLevel level, List<ServerPlayer> players) {
        if (players.isEmpty()) return;
        String dim = level.dimension().identifier().toString();
        for (ServerPlayer player : players) {
            if (!level.dimension().equals(player.level().dimension())) continue;
            tryRegister(level, dim, player);
        }
    }

    private static void tryRegister(ServerLevel level, String dim, ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(SCAN_RADIUS);
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, box, Villager::isAlive);
        if (villagers.size() < MIN_VILLAGER_CLUSTER) return;

        double cx = 0, cy = 0, cz = 0;
        for (Villager v : villagers) {
            cx += v.getX();
            cy += v.getY();
            cz += v.getZ();
        }
        cx /= villagers.size();
        cy /= villagers.size();
        cz /= villagers.size();
        int ix = (int) Math.round(cx);
        int iz = (int) Math.round(cz);
        int iy = Build.groundY(level, ix, iz);

        long clusterKey = clusterKey(dim, ix, iz);
        if (REGISTERED_CLUSTERS.contains(clusterKey)) return;

        Settlement near = DataManager.nearestSettlement(dim, ix, iz);
        if (near != null && near.distSq(ix, iz) < MIN_SETTLEMENT_SEP * MIN_SETTLEMENT_SEP) return;

        Random rng = new Random(((long) ix << 32) ^ iz);
        String name = SettlementGenerator.pickName(rng);
        String id = "village_" + ix + "_" + iz;
        Settlement s = new Settlement(id, name, SettlementType.VILLAGE, ix, iy, iz, dim);
        DataManager.addSettlement(s);
        REGISTERED_CLUSTERS.add(clusterKey);

        BlockPos marker = findBellOrCenter(level, ix, iy, iz);
        if (level.getBlockState(marker).isAir()) {
            Build.set(level, marker.getX(), marker.getY(), marker.getZ(), ModBlocks.CIVIC_MARKER);
        } else {
            Build.set(level, marker.getX(), marker.above().getY(), marker.getZ(), ModBlocks.CIVIC_MARKER);
        }

        player.sendSystemMessage(Component.literal("You have discovered the village of " + name + ".")
                .withStyle(SettlementType.VILLAGE.color, ChatFormatting.BOLD));
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("The village of " + name + " is now under local governance.")
                        .withStyle(ChatFormatting.GRAY), false);
    }

    private static BlockPos findBellOrCenter(ServerLevel level, int cx, int cy, int cz) {
        int r = 24;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -6; dy <= 8; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                    if (level.getBlockState(p).is(Blocks.BELL)) return p.above();
                }
            }
        }
        return new BlockPos(cx, cy + 1, cz);
    }

    private static long clusterKey(String dim, int x, int z) {
        int cell = 64;
        return (((long) dim.hashCode()) << 32)
                ^ ((long) Math.floorDiv(x, cell) << 16)
                ^ Math.floorDiv(z, cell);
    }

    /** Rebuild cluster cache from persisted settlements after server load. */
    public static void onServerStarted() {
        REGISTERED_CLUSTERS.clear();
        for (Settlement s : DataManager.settlements().values()) {
            if (s.type == SettlementType.VILLAGE) {
                REGISTERED_CLUSTERS.add(clusterKey(s.dimension, s.x, s.z));
            }
        }
    }
}
