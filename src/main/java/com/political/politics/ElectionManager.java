package com.political.politics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Elections, term scheduling, and impeachment votes. */
public final class ElectionManager {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final long WEEK_MS = 7 * DAY_MS;
    private static final int MAX_CANDIDATES = 5;

    private ElectionManager() {}

    public static boolean isElectionActive() {
        return DataManager.data().electionActive;
    }

    public static boolean isImpeachmentActive() {
        return DataManager.data().impeachmentActive;
    }

    public static void tick(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        long now = System.currentTimeMillis();
        if (d.electionActive) {
            if (now >= d.electionEndTime) endElection(server);
        } else if (d.electionSystemEnabled && !d.dictatorActive && d.termEndTime != 0 && now >= d.termEndTime) {
            startElection(server);
        }
    }

    public static void startElection(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        // Hybrid model: only settlement Leaders may stand for the national Chair.
        // Fall back to the whole roster if no Leaders have emerged yet.
        List<String> pool = new ArrayList<>();
        for (Settlement s : d.settlements.values()) {
            if (!s.leader.isEmpty() && !pool.contains(s.leader)) pool.add(s.leader);
        }
        if (pool.isEmpty()) pool = new ArrayList<>(d.playerNames.keySet());
        Collections.shuffle(pool);
        d.candidates = new ArrayList<>(pool.subList(0, Math.min(MAX_CANDIDATES, pool.size())));
        d.votes.clear();
        d.votedPlayers.clear();
        for (String c : d.candidates) d.votes.put(c, 0);
        d.electionActive = true;
        d.electionEndTime = System.currentTimeMillis() + DAY_MS;

        Component msg = Component.literal("An election has begun! Use /vote <player>. Candidates: ")
                .withStyle(ChatFormatting.GOLD);
        StringBuilder sb = new StringBuilder();
        for (String c : d.candidates) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(DataManager.nameOf(c));
        }
        broadcast(server, msg.copy().append(Component.literal(sb.toString()).withStyle(ChatFormatting.YELLOW)));
    }

    public static void forceStartElection(MinecraftServer server) {
        DataManager.data().electionActive = false;
        startElection(server);
    }

    public static boolean castVote(ServerPlayer voter, String candidateUuid) {
        PoliticsData d = DataManager.data();
        if (!d.electionActive) return false;
        String voterId = voter.getStringUUID();
        if (voterId.equals(candidateUuid)) return false;
        if (d.votedPlayers.containsKey(voterId)) return false;
        if (!d.candidates.contains(candidateUuid)) return false;
        d.votedPlayers.put(voterId, candidateUuid);
        d.votes.merge(candidateUuid, 1, Integer::sum);
        return true;
    }

    public static void endElection(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        d.electionActive = false;

        List<Map.Entry<String, Integer>> ranked = new ArrayList<>(d.votes.entrySet());
        ranked.sort(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue()).reversed());

        String newChair = ranked.isEmpty() ? "" : ranked.get(0).getKey();
        String newVice = ranked.size() > 1 ? ranked.get(1).getKey() : "";

        if (newChair.equals(d.chair) && !newChair.isEmpty()) {
            d.chairTermCount++;
        } else {
            d.chairTermCount = newChair.isEmpty() ? 0 : 1;
            PerkManager.clearAllPerks();
        }
        DataManager.setChair(newChair);
        DataManager.setViceChair(newVice);
        d.impeachmentActive = false;
        d.termEndTime = System.currentTimeMillis() + WEEK_MS;

        broadcast(server, Component.literal("Election concluded. New Chair: " + DataManager.nameOf(newChair)
                + (newVice.isEmpty() ? "" : ", Vice Chair: " + DataManager.nameOf(newVice)))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    // --- Impeachment ---

    public static void startImpeachment(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        if (d.chair.isEmpty()) return;
        d.impeachmentActive = true;
        d.impeachYes = 0;
        d.impeachNo = 0;
        d.impeachVoted.clear();
        broadcast(server, Component.literal("Impeachment proceedings have begun against Chair "
                + DataManager.nameOf(d.chair) + ". Use /impeach vote <yes|no>.").withStyle(ChatFormatting.RED));
    }

    public static boolean castImpeachVote(ServerPlayer voter, boolean yes) {
        PoliticsData d = DataManager.data();
        if (!d.impeachmentActive) return false;
        String id = voter.getStringUUID();
        if (d.impeachVoted.contains(id)) return false;
        d.impeachVoted.add(id);
        if (yes) d.impeachYes++;
        else d.impeachNo++;
        return true;
    }

    public static void resolveImpeachment(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        if (!d.impeachmentActive) return;
        int total = d.impeachYes + d.impeachNo;
        if (total < 5) return;
        d.impeachmentActive = false;
        if ((double) d.impeachYes / total >= 0.666) {
            PerkManager.clearAllPerks();
            DataManager.setChair("");
            DataManager.setViceChair("");
            broadcast(server, Component.literal("The Chair has been impeached! Calling an emergency election.")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            forceStartElection(server);
        } else {
            broadcast(server, Component.literal("Impeachment failed. The Chair remains in office.")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    public static long remainingMillis() {
        PoliticsData d = DataManager.data();
        return d.electionActive ? Math.max(0, d.electionEndTime - System.currentTimeMillis())
                : Math.max(0, d.termEndTime - System.currentTimeMillis());
    }

    private static void broadcast(MinecraftServer server, Component msg) {
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }
}
