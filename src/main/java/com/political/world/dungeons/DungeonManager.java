package com.political.world.dungeons;

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
 * Scatters dungeons near exploring players and streams block placement over ticks.
 * Mirrors the settlement build queue pattern without mixins.
 */
public final class DungeonManager {

    private static final int PLACE_BUDGET_PER_TICK = 12000;
    private static final int SCATTER_INTERVAL = 80;

    private static long lastScatterTick = 0L;
    private static final java.util.ArrayDeque<Pending> PENDING = new java.util.ArrayDeque<>();
    private static final List<DungeonSite> SITES = Collections.synchronizedList(new ArrayList<>());
    private static final Set<String> GENERATED_CELLS = new HashSet<>();

    private static final class Pending {
        final ServerLevel level;
        final BuildBuffer buffer;
        final DungeonPlan plan;
        int cursor = 0;

        Pending(ServerLevel level, BuildBuffer buffer, DungeonPlan plan) {
            this.level = level;
            this.buffer = buffer;
            this.plan = plan;
        }
    }

    private DungeonManager() {}

    public static void register() {
        // no block callbacks yet
    }

    public static void tick(MinecraftServer server) {
        drainPending();
        if (!PENDING.isEmpty()) return;

        // Auto-scatter can be disabled by config; queued dungeons (above) still finish building.
        if (!com.political.config.PoliticalConfig.get().dungeonsEnabled) return;

        long tick = server.overworld().getGameTime();
        if (tick - lastScatterTick < SCATTER_INTERVAL) return;
        lastScatterTick = tick;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) continue;
            if (tryScatterNear(level, player.getBlockX(), player.getBlockZ())) break;
        }
    }

    /** Admin / command summon at coordinates. */
    public static DungeonSite queueAt(ServerLevel level, int x, int z, DungeonType type) {
        Random rng = new Random(type.id.hashCode() ^ ((long) x << 32) ^ z);
        BuildBuffer buffer = new BuildBuffer();
        DungeonPlan plan;
        try {
            plan = DungeonGenerator.planInto(buffer, level, x, z, type, rng);
        } catch (RuntimeException e) {
            RpgPoliticsMod.LOGGER.error("Dungeon generation failed at {},{}", x, z, e);
            throw e;
        }
        PENDING.addLast(new Pending(level, buffer, plan));
        SITES.add(plan.site);
        return plan.site;
    }

    public static List<DungeonSite> sites() {
        return List.copyOf(SITES);
    }

    public static DungeonSite nearest(ServerLevel level, double x, double z) {
        String dim = level.dimension().identifier().toString();
        DungeonSite best = null;
        double bestD = Double.MAX_VALUE;
        for (DungeonSite s : SITES) {
            if (!dim.equals(s.dimension)) continue;
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
            List<BuildBuffer.Op> ops = p.buffer.ops;
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

    private static void finish(ServerLevel level, DungeonPlan plan) {
        DungeonLoot.applyPostOps(level, plan);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("A " + plan.site.type.display + " has been discovered near "
                        + plan.site.x + ", " + plan.site.y + ", " + plan.site.z + ".")
                        .withStyle(plan.site.type.color, ChatFormatting.BOLD), false);
        RpgPoliticsMod.LOGGER.info("Dungeon {} finished at {}, {}, {}", plan.site.type.id,
                plan.site.x, plan.site.y, plan.site.z);
    }

    private static boolean tryScatterNear(ServerLevel level, int px, int pz) {
        int cell = 128;
        String key = level.dimension().identifier() + "|" + Math.floorDiv(px, cell) + "|" + Math.floorDiv(pz, cell);
        if (GENERATED_CELLS.contains(key)) return false;
        GENERATED_CELLS.add(key);

        Random rng = new Random(key.hashCode() ^ 0xD09E901L);
        if (rng.nextDouble() > com.political.config.PoliticalConfig.get().dungeonSpawnRate) return false;

        double angle = rng.nextDouble() * Math.PI * 2;
        int dist = 48 + rng.nextInt(64);
        int cx = px + (int) Math.round(Math.cos(angle) * dist);
        int cz = pz + (int) Math.round(Math.sin(angle) * dist);

        if (!level.isLoaded(new BlockPos(cx, level.getSeaLevel(), cz))) return false;

        DungeonSite near = nearest(level, cx, cz);
        if (near != null && near.distSq(cx, cz) < 70.0 * 70.0) return false;

        DungeonType type = DungeonType.roll(rng);
        queueAt(level, cx, cz, type);
        return true;
    }
}
