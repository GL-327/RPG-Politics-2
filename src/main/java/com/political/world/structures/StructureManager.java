package com.political.world.structures;

import com.political.RpgPoliticsMod;
import com.political.world.BuildBuffer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Scatters above-ground RPG structures near exploring players and streams their block placement
 * over ticks — the same mixin-free build-queue pattern used by {@code SettlementManager} and
 * {@code DungeonManager}. Tracks placed sites for the {@code /structure} command set and avoids
 * crowding existing structures, settlements and other sites.
 */
public final class StructureManager {

    private static final int PLACE_BUDGET_PER_TICK = 9000;
    private static final int SCATTER_INTERVAL = 100;
    private static final double SCATTER_CHANCE = 0.18;
    private static final int CELL = 144;
    private static final double MIN_GAP = 80.0;

    private static long lastScatterTick = 0L;
    private static final java.util.ArrayDeque<Pending> PENDING = new java.util.ArrayDeque<>();
    private static final List<StructureSite> SITES = Collections.synchronizedList(new ArrayList<>());
    private static final Set<String> GENERATED_CELLS = new HashSet<>();

    private static final class Pending {
        final ServerLevel level;
        final StructurePlan plan;
        int cursor = 0;
        Pending(ServerLevel level, StructurePlan plan) {
            this.level = level;
            this.plan = plan;
        }
    }

    private StructureManager() {}

    public static void register() {
        // No block callbacks; scatter + streaming are driven from tick().
    }

    public static void tick(MinecraftServer server) {
        if (!com.political.config.PoliticalConfig.get().proceduralStructuresEnabled) return;
        drainPending();
        if (!PENDING.isEmpty()) return;

        long tick = server.overworld().getGameTime();
        if (tick - lastScatterTick < SCATTER_INTERVAL) return;
        lastScatterTick = tick;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) continue;
            if (tryScatterNear(level, player.getBlockX(), player.getBlockZ())) break;
        }
    }

    /** Admin / command summon at coordinates. */
    public static StructureSite queueAt(ServerLevel level, int x, int z, StructureType type) {
        Random rng = new Random(type.id.hashCode() ^ ((long) x << 32) ^ z);
        BuildBuffer buffer = new BuildBuffer();
        StructurePlan plan;
        try {
            plan = StructureGenerator.planInto(buffer, level, x, z, type, rng);
        } catch (RuntimeException e) {
            RpgPoliticsMod.LOGGER.error("Surface structure generation failed at {},{}", x, z, e);
            throw e;
        }
        PENDING.addLast(new Pending(level, plan));
        SITES.add(plan.site);
        return plan.site;
    }

    public static List<StructureSite> sites() {
        return List.copyOf(SITES);
    }

    public static StructureSite nearest(ServerLevel level, double x, double z) {
        String dim = level.dimension().identifier().toString();
        StructureSite best = null;
        double bestD = Double.MAX_VALUE;
        for (StructureSite s : SITES) {
            if (!dim.equals(s.dimension)) continue;
            double d = s.distSq(x, z);
            if (d < bestD) { bestD = d; best = s; }
        }
        return best;
    }

    public static StructureSite nearestOfType(ServerLevel level, double x, double z, StructureType type) {
        String dim = level.dimension().identifier().toString();
        StructureSite best = null;
        double bestD = Double.MAX_VALUE;
        for (StructureSite s : SITES) {
            if (!dim.equals(s.dimension) || s.type != type) continue;
            double d = s.distSq(x, z);
            if (d < bestD) { bestD = d; best = s; }
        }
        return best;
    }

    private static void drainPending() {
        int budget = PLACE_BUDGET_PER_TICK;
        while (budget > 0 && !PENDING.isEmpty()) {
            Pending p = PENDING.peekFirst();
            List<BuildBuffer.Op> ops = p.plan.buffer.ops;
            while (budget > 0 && p.cursor < ops.size()) {
                BuildBuffer.Op op = ops.get(p.cursor++);
                p.level.setBlock(new BlockPos(op.x, op.y, op.z), op.state, 2);
                budget--;
            }
            if (p.cursor >= ops.size()) {
                PENDING.removeFirst();
                finish(p.level, p.plan);
            }
        }
    }

    private static void finish(ServerLevel level, StructurePlan plan) {
        StructureLoot.applyPostOps(level, plan);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("A " + plan.site.type.display + " has been discovered near "
                        + plan.site.x + ", " + plan.site.y + ", " + plan.site.z + ".")
                        .withStyle(plan.site.type.color, ChatFormatting.BOLD), false);
        RpgPoliticsMod.LOGGER.info("Surface structure {} finished at {}, {}, {}", plan.site.type.id,
                plan.site.x, plan.site.y, plan.site.z);
    }

    private static boolean tryScatterNear(ServerLevel level, int px, int pz) {
        String key = level.dimension().identifier() + "|" + Math.floorDiv(px, CELL) + "|" + Math.floorDiv(pz, CELL);
        if (GENERATED_CELLS.contains(key)) return false;
        GENERATED_CELLS.add(key);

        Random rng = new Random(key.hashCode() ^ 0x57A12C7L);
        if (rng.nextDouble() > SCATTER_CHANCE) return false;

        double angle = rng.nextDouble() * Math.PI * 2;
        int dist = 56 + rng.nextInt(72);
        int cx = px + (int) Math.round(Math.cos(angle) * dist);
        int cz = pz + (int) Math.round(Math.sin(angle) * dist);

        if (!level.isLoaded(new BlockPos(cx, level.getSeaLevel(), cz))) return false;

        // Don't crowd other surface sites.
        StructureSite near = nearest(level, cx, cz);
        if (near != null && near.distSq(cx, cz) < MIN_GAP * MIN_GAP) return false;

        // Don't drop onto a settlement.
        try {
            var settle = com.political.politics.DataManager.nearestSettlement(
                    level.dimension().identifier().toString(), cx, cz);
            if (settle != null && settle.distSq(cx, cz) < 96.0 * 96.0) return false;
        } catch (Throwable ignored) {
            // Politics data not ready yet; proceed without the settlement guard.
        }

        StructureType type = StructureType.roll(rng);
        queueAt(level, cx, cz, type);
        return true;
    }
}
