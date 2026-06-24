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
 * Auto-scatters the content workstream's original surface structures ({@link ContentStructureKind})
 * near exploring players, using the same deferred build-queue pattern as {@link StructureManager}.
 */
public final class ContentStructureManager {

    private static final int PLACE_BUDGET_PER_TICK = 6000;
    private static final int SCATTER_INTERVAL = 120;
    private static final double SCATTER_CHANCE = 0.14;
    private static final int CELL = 160;
    private static final double MIN_GAP = 96.0;

    private static long lastScatterTick = 0L;
    private static final java.util.ArrayDeque<Pending> PENDING = new java.util.ArrayDeque<>();
    private static final List<ContentSite> SITES = Collections.synchronizedList(new ArrayList<>());
    private static final Set<String> GENERATED_CELLS = new HashSet<>();

    public record ContentSite(String dimension, int x, int y, int z, ContentStructureKind kind, long placedAt) {
        public double distSq(double px, double pz) {
            double dx = x - px;
            double dz = z - pz;
            return dx * dx + dz * dz;
        }
    }

    private static final class Pending {
        final ServerLevel level;
        final StructurePlan plan;
        final ContentStructureKind kind;
        int cursor = 0;

        Pending(ServerLevel level, StructurePlan plan, ContentStructureKind kind) {
            this.level = level;
            this.plan = plan;
            this.kind = kind;
        }
    }

    private ContentStructureManager() {}

    public static void tick(MinecraftServer server) {
        if (!com.political.config.PoliticalConfig.get().contentProceduralStructuresEnabled) return;
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

    public static ContentSite queueAt(ServerLevel level, int x, int z, ContentStructureKind kind) {
        Random rng = new Random(kind.id.hashCode() ^ ((long) x << 32) ^ z);
        BuildBuffer buffer = new BuildBuffer();
        StructurePlan plan = ContentStructures.planInto(buffer, level, x, z, kind, rng);
        PENDING.addLast(new Pending(level, plan, kind));
        ContentSite site = new ContentSite(
                level.dimension().identifier().toString(), plan.site.x, plan.site.y, plan.site.z,
                kind, System.currentTimeMillis());
        SITES.add(site);
        return site;
    }

    public static List<ContentSite> sites() {
        return List.copyOf(SITES);
    }

    public static ContentSite nearest(ServerLevel level, double x, double z) {
        String dim = level.dimension().identifier().toString();
        ContentSite best = null;
        double bestD = Double.MAX_VALUE;
        for (ContentSite s : SITES) {
            if (!dim.equals(s.dimension())) continue;
            double d = s.distSq(x, z);
            if (d < bestD) {
                bestD = d;
                best = s;
            }
        }
        return best;
    }

    public static ContentSite nearestOfKind(ServerLevel level, double x, double z, ContentStructureKind kind) {
        String dim = level.dimension().identifier().toString();
        ContentSite best = null;
        double bestD = Double.MAX_VALUE;
        for (ContentSite s : SITES) {
            if (!dim.equals(s.dimension()) || s.kind() != kind) continue;
            double d = s.distSq(x, z);
            if (d < bestD) {
                bestD = d;
                best = s;
            }
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
                finish(p.level, p.plan, p.kind);
            }
        }
    }

    private static void finish(ServerLevel level, StructurePlan plan, ContentStructureKind kind) {
        StructureLoot.applyPostOps(level, plan);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("A " + kind.display + " has been discovered near "
                        + plan.site.x + ", " + plan.site.y + ", " + plan.site.z + ".")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        RpgPoliticsMod.LOGGER.info("Content structure {} finished at {}, {}, {}",
                kind.id, plan.site.x, plan.site.y, plan.site.z);
    }

    private static boolean tryScatterNear(ServerLevel level, int px, int pz) {
        String key = level.dimension().identifier() + "|content|" + Math.floorDiv(px, CELL) + "|" + Math.floorDiv(pz, CELL);
        if (GENERATED_CELLS.contains(key)) return false;
        GENERATED_CELLS.add(key);

        Random rng = new Random(key.hashCode() ^ 0xC0FFEE01L);
        if (rng.nextDouble() > SCATTER_CHANCE) return false;

        double angle = rng.nextDouble() * Math.PI * 2;
        int dist = 64 + rng.nextInt(80);
        int cx = px + (int) Math.round(Math.cos(angle) * dist);
        int cz = pz + (int) Math.round(Math.sin(angle) * dist);

        if (!level.isLoaded(new BlockPos(cx, level.getSeaLevel(), cz))) return false;

        ContentSite near = nearest(level, cx, cz);
        if (near != null && near.distSq(cx, cz) < MIN_GAP * MIN_GAP) return false;

        StructureSite legacyNear = StructureManager.nearest(level, cx, cz);
        if (legacyNear != null && legacyNear.distSq(cx, cz) < MIN_GAP * MIN_GAP) return false;

        try {
            var settle = com.political.politics.DataManager.nearestSettlement(
                    level.dimension().identifier().toString(), cx, cz);
            if (settle != null && settle.distSq(cx, cz) < 96.0 * 96.0) return false;
        } catch (Throwable ignored) {
        }

        ContentStructureKind kind = ContentStructureKind.values()[rng.nextInt(ContentStructureKind.values().length)];
        queueAt(level, cx, cz, kind);
        return true;
    }
}
