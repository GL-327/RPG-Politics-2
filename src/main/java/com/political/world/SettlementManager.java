package com.political.world;

import com.political.politics.CivicRank;
import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import com.political.politics.Settlement;
import com.political.politics.SettlementType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Drives settlement generation and political geography: builds the capital at world
 * spawn, scatters more settlements as players explore, enrols citizens, and runs each
 * settlement's rank-gated local election.
 */
public final class SettlementManager {

    private static long lastScatterTick = 0L;

    private SettlementManager() {}

    /** Right-clicking a Civic Marker opens the local political readout. */
    public static void register() {
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide() || hand != net.minecraft.world.InteractionHand.MAIN_HAND) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            if (!(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel level)) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            BlockPos pos = hit.getBlockPos();
            if (level.getBlockState(pos).getBlock() != com.political.content.ModBlocks.CIVIC_MARKER) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            Settlement s = nearestSettlement(level, pos.getX(), pos.getZ());
            if (s == null) return net.minecraft.world.InteractionResult.PASS;
            showCivicReadout(sp, s);
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
    }

    private static void showCivicReadout(ServerPlayer p, Settlement s) {
        p.sendSystemMessage(Component.literal("\u2014 " + s.name + " (" + s.type.display + ") \u2014")
                .withStyle(s.type.color, ChatFormatting.BOLD));
        p.sendSystemMessage(Component.literal("Leader: " + (s.leader.isEmpty() ? "vacant" : DataManager.nameOf(s.leader))
                + (s.governedBy.isEmpty() ? "  (sovereign)" : "  (under " + nameOf(s.governedBy) + ")"))
                .withStyle(ChatFormatting.GRAY));
        CivicRank r = DataManager.civicRank(p.getStringUUID());
        boolean citizen = s.id.equals(DataManager.citizenshipOf(p.getStringUUID()));
        p.sendSystemMessage(Component.literal("You: " + (citizen ? "citizen, " : "visitor, ") + "rank " + r.display
                + (DataManager.canStandForElection(p.getStringUUID()) ? " (eligible for Leader)" : ""))
                .withStyle(r.color));
        p.sendSystemMessage(Component.literal("Use /settlement advance to climb the ranks, /settlement here for details.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String nameOf(String id) {
        Settlement s = DataManager.settlement(id);
        return s == null ? id : s.name;
    }

    // ------------------------------------------------------------------
    // World spawn capital
    // ------------------------------------------------------------------

    public static void onServerStarted(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        if (!d.capitalId.isEmpty() && DataManager.settlement(d.capitalId) != null) return;

        try {
            ServerLevel level = server.overworld();
            BlockPos spawn = level.getRespawnData().pos();
            int cx = spawn.getX();
            int cz = spawn.getZ();

            // Make sure the spawn chunk is generated so heightmaps are valid.
            level.getChunk(cx >> 4, cz >> 4);

            Random rng = new Random(((long) cx << 32) ^ cz);
            Settlement capital = SettlementGenerator.generateCapital(level, cx, cz, SettlementGenerator.pickName(rng));
            d.capitalId = capital.id;
            markCell(d, level, cx, cz);

            // Relocate world spawn just inside the gate of the capital, facing the keep.
            BlockPos newSpawn = new BlockPos(cx, capital.y + 1, cz + 18);
            level.setRespawnData(net.minecraft.world.level.storage.LevelData.RespawnData.of(
                    level.dimension(), newSpawn, 180.0f, 0.0f));

            com.political.RpgPoliticsMod.LOGGER.info("Founded capital '{}' at {},{}", capital.name, cx, cz);
        } catch (RuntimeException e) {
            com.political.RpgPoliticsMod.LOGGER.error("Failed to found the capital at world spawn", e);
        }
    }

    // ------------------------------------------------------------------
    // Exploration scatter + local elections
    // ------------------------------------------------------------------

    public static void tick(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        long now = System.currentTimeMillis();

        // Local elections every server tick (cheap; guarded inside).
        tickElections(server, d, now);

        // Scatter at most one new settlement every ~3 seconds.
        if (!d.settlementGenEnabled) return;
        long tick = server.overworld().getGameTime();
        if (tick - lastScatterTick < 60) return;
        lastScatterTick = tick;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) continue;
            if (tryScatterAround(level, d, player.getBlockX(), player.getBlockZ())) break; // one per pass
        }

        // Keep online players enrolled as citizens of their nearest settlement.
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ensureCitizenship(player);
        }
    }

    private static int cellSize(PoliticsData d) {
        return Math.max(16, d.settlementGridChunks * 16);
    }

    private static String cellKey(ServerLevel level, int cx, int cz, int cell) {
        return level.dimension().identifier() + "|" + Math.floorDiv(cx, cell) + "|" + Math.floorDiv(cz, cell);
    }

    private static void markCell(PoliticsData d, ServerLevel level, int x, int z) {
        int cell = cellSize(d);
        String key = cellKey(level, x, z, cell);
        if (!d.generatedCells.contains(key)) d.generatedCells.add(key);
    }

    private static boolean tryScatterAround(ServerLevel level, PoliticsData d, int px, int pz) {
        int cell = cellSize(d);
        String key = cellKey(level, px, pz, cell);
        if (d.generatedCells.contains(key)) return false;
        d.generatedCells.add(key);

        int gx = Math.floorDiv(px, cell);
        int gz = Math.floorDiv(pz, cell);
        Random rng = new Random((((long) gx * 73428767L) ^ ((long) gz * 912931L)) ^ 0x5DEECE66DL);
        if (rng.nextDouble() > d.settlementSpawnChance) return false;

        // Place the settlement a short, deterministic distance from the player so it always
        // lands in already-loaded chunks (correct heightmap, no floating/buried builds).
        double angle = rng.nextDouble() * Math.PI * 2;
        int dist = 64 + rng.nextInt(48);
        int cx = px + (int) Math.round(Math.cos(angle) * dist);
        int cz = pz + (int) Math.round(Math.sin(angle) * dist);

        if (!level.isLoaded(new BlockPos(cx, level.getSeaLevel(), cz))) return false;

        // Don't crowd existing settlements.
        Settlement near = DataManager.nearestSettlement(level.dimension().identifier().toString(), cx, cz);
        if (near != null && near.distSq(cx, cz) < 90.0 * 90.0) return false;

        SettlementType type = rollType(rng);
        String name = SettlementGenerator.pickName(rng);
        Settlement s;
        try {
            s = SettlementGenerator.generate(level, cx, cz, type, name);
        } catch (RuntimeException e) {
            com.political.RpgPoliticsMod.LOGGER.error("Settlement generation failed at {},{}", cx, cz, e);
            return false;
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("A new " + type.display.toLowerCase() + ", " + name
                        + (s.governedBy.isEmpty() ? "" : " (under " + DataManager.settlement(s.governedBy).name + ")")
                        + ", has risen at " + cx + ", " + cz + ".").withStyle(type.color), false);
        return true;
    }

    private static SettlementType rollType(Random rng) {
        double r = rng.nextDouble();
        if (r < 0.06) return SettlementType.CAPITAL;
        if (r < 0.20) return SettlementType.CITY;
        if (r < 0.50) return SettlementType.TOWN;
        return SettlementType.VILLAGE;
    }

    // ------------------------------------------------------------------
    // Citizenship
    // ------------------------------------------------------------------

    public static void ensureCitizenship(ServerPlayer player) {
        String uuid = player.getStringUUID();
        if (DataManager.citizenshipOf(uuid) != null) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        Settlement s = DataManager.nearestSettlement(level.dimension().identifier().toString(),
                player.getBlockX(), player.getBlockZ());
        if (s == null) return;
        DataManager.setCitizenship(uuid, s.id);
        DataManager.setCivicRank(uuid, CivicRank.CITIZEN);
        player.sendSystemMessage(Component.literal("You are now a citizen of " + s.name + " (" + s.type.display + ").")
                .withStyle(s.type.color));
    }

    /** Returns the nearest settlement's name for an entity position, or null. */
    public static Settlement nearestSettlement(ServerLevel level, double x, double z) {
        return DataManager.nearestSettlement(level.dimension().identifier().toString(), (int) x, (int) z);
    }

    // ------------------------------------------------------------------
    // Local (per-settlement) elections — rank-gated candidacy
    // ------------------------------------------------------------------

    private static void tickElections(MinecraftServer server, PoliticsData d, long now) {
        for (Settlement s : d.settlements.values()) {
            if (s.electionActive) {
                if (now >= s.electionEnd) endElection(server, s);
                continue;
            }
            // Auto-start a local election when the seat is vacant or the term has expired,
            // provided at least one citizen has climbed to Councilor (the candidacy gate).
            boolean termOver = s.leader.isEmpty() || (s.nextElection != 0L && now >= s.nextElection);
            if (termOver && !eligibleCandidates(s).isEmpty()) {
                startElection(server, s);
            }
        }
    }

    public static List<String> eligibleCandidates(Settlement s) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, String> e : DataManager.data().citizenship.entrySet()) {
            if (!s.id.equals(e.getValue())) continue;
            if (DataManager.canStandForElection(e.getKey())) out.add(e.getKey());
        }
        return out;
    }

    public static boolean startElection(MinecraftServer server, Settlement s) {
        List<String> pool = eligibleCandidates(s);
        if (pool.isEmpty()) return false;

        // Uncontested: the sole eligible Councilor takes office immediately.
        if (pool.size() == 1) {
            s.electionActive = false;
            s.leader = pool.get(0);
            s.nextElection = System.currentTimeMillis() + DataManager.data().settlementTermMillis;
            server.getPlayerList().broadcastSystemMessage(Component.literal(
                    DataManager.nameOf(s.leader) + " has become Leader of " + s.name + " unopposed.")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
            return true;
        }

        s.candidates = new ArrayList<>(pool);
        s.votes.clear();
        s.voted.clear();
        for (String c : s.candidates) s.votes.put(c, 0);
        s.electionActive = true;
        s.electionEnd = System.currentTimeMillis() + 10L * 60 * 1000; // 10-minute voting window
        StringBuilder sb = new StringBuilder();
        for (String c : s.candidates) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(DataManager.nameOf(c));
        }
        server.getPlayerList().broadcastSystemMessage(Component.literal(
                "Election for Leader of " + s.name + " has begun. Councilors standing: " + sb
                        + ". Use /settlement vote " + s.id + " <player>.").withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    public static boolean castVote(Settlement s, ServerPlayer voter, String candidateUuid) {
        if (!s.electionActive) return false;
        String id = voter.getStringUUID();
        if (!s.id.equals(DataManager.citizenshipOf(id))) return false; // only citizens vote
        if (s.voted.contains(id)) return false;
        if (!s.candidates.contains(candidateUuid)) return false;
        s.voted.add(id);
        s.votes.merge(candidateUuid, 1, Integer::sum);
        return true;
    }

    public static void endElection(MinecraftServer server, Settlement s) {
        s.electionActive = false;
        String winner = s.votes.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey).orElse("");
        s.leader = winner;
        s.nextElection = System.currentTimeMillis() + DataManager.data().settlementTermMillis;
        server.getPlayerList().broadcastSystemMessage(Component.literal(
                "New Leader of " + s.name + ": " + (winner.isEmpty() ? "vacant" : DataManager.nameOf(winner)))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
    }
}
