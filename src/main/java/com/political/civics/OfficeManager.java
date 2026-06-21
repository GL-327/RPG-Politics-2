package com.political.civics;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Multi-office elections with campaigns: candidates self-nominate with a manifesto
 * and may pledge campaign funds (which boost their effective vote and flow to the
 * treasury). One office is contested at a time.
 */
public final class OfficeManager {

    private static final long CAMPAIGN_MS = 10L * 60 * 1000; // 10-minute race window
    private static final int FUNDS_PER_VOTE = 500;           // coins pledged per +1 effective vote
    private static final int MAX_FUNDED_VOTES = 5;

    private OfficeManager() {}

    public static String holder(CivicOffice office) {
        return DataManager.data().civicOffices.getOrDefault(office.id, "");
    }

    public static boolean holds(String uuid, CivicOffice office) {
        return uuid != null && uuid.equals(holder(office));
    }

    public static boolean holdsAny(String uuid) {
        for (CivicOffice o : CivicOffice.values()) {
            if (holds(uuid, o)) return true;
        }
        return false;
    }

    public static void setHolder(CivicOffice office, String uuid) {
        DataManager.data().civicOffices.put(office.id, uuid == null ? "" : uuid);
    }

    public static boolean isElectionActive() {
        return DataManager.data().officeElectionActive;
    }

    public static CivicOffice contestedOffice() {
        return CivicOffice.byId(DataManager.data().officeElectionOffice);
    }

    /** Opens a campaign+voting window for an office. */
    public static boolean startElection(MinecraftServer server, CivicOffice office) {
        PoliticsData d = DataManager.data();
        if (d.officeElectionActive) return false;
        d.officeElectionActive = true;
        d.officeElectionOffice = office.id;
        d.officeElectionEnd = System.currentTimeMillis() + CAMPAIGN_MS;
        d.officeCandidates.clear();
        d.officeVotes.clear();
        d.officeVoted.clear();
        d.campaignFunds.clear();
        broadcast(server, Component.literal("Campaign season! The office of ")
                .withStyle(ChatFormatting.GOLD)
                .copy().append(Component.literal(office.displayName).withStyle(office.color, ChatFormatting.BOLD))
                .append(Component.literal(" is open. Use /office stand <manifesto> to run, then /office vote <player>.")
                        .withStyle(ChatFormatting.YELLOW)));
        return true;
    }

    /** Self-nominate with a manifesto. Requires an active race. */
    public static boolean stand(ServerPlayer player, String manifesto) {
        PoliticsData d = DataManager.data();
        if (!d.officeElectionActive) return false;
        String uuid = player.getStringUUID();
        if (!d.officeCandidates.contains(uuid)) {
            d.officeCandidates.add(uuid);
            d.officeVotes.putIfAbsent(uuid, 0);
        }
        d.manifestos.put(uuid, manifesto == null ? "" : manifesto);
        return true;
    }

    /** Pledge campaign funds (spent immediately to the treasury) to boost reach. */
    public static boolean pledge(ServerPlayer player, int coins) {
        PoliticsData d = DataManager.data();
        if (!d.officeElectionActive) return false;
        String uuid = player.getStringUUID();
        if (!d.officeCandidates.contains(uuid)) return false;
        if (!DataManager.removeCoins(uuid, coins)) return false;
        DataManager.addTreasury(coins);
        d.campaignFunds.merge(uuid, coins, Integer::sum);
        return true;
    }

    public static boolean vote(ServerPlayer voter, String candidateUuid) {
        PoliticsData d = DataManager.data();
        if (!d.officeElectionActive) return false;
        String voterId = voter.getStringUUID();
        if (d.officeVoted.contains(voterId)) return false;
        if (!d.officeCandidates.contains(candidateUuid)) return false;
        d.officeVoted.add(voterId);
        d.officeVotes.merge(candidateUuid, 1, Integer::sum);
        return true;
    }

    private static int effectiveVotes(String uuid) {
        PoliticsData d = DataManager.data();
        int base = d.officeVotes.getOrDefault(uuid, 0);
        int funded = Math.min(MAX_FUNDED_VOTES, d.campaignFunds.getOrDefault(uuid, 0) / FUNDS_PER_VOTE);
        return base + funded;
    }

    public static void tick(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        if (!d.officeElectionActive) return;
        if (System.currentTimeMillis() < d.officeElectionEnd) return;
        endElection(server);
    }

    public static void endElection(MinecraftServer server) {
        PoliticsData d = DataManager.data();
        if (!d.officeElectionActive) return;
        CivicOffice office = CivicOffice.byId(d.officeElectionOffice);
        d.officeElectionActive = false;
        if (office == null) return;

        List<String> ranked = new ArrayList<>(d.officeCandidates);
        ranked.sort(Comparator.comparingInt(OfficeManager::effectiveVotes).reversed());
        String winner = ranked.isEmpty() ? "" : ranked.get(0);
        setHolder(office, winner);

        if (winner.isEmpty()) {
            broadcast(server, Component.literal("The race for " + office.displayName + " drew no candidates.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        // Winning party gains influence.
        FactionManager.addInfluence(winner, 25);
        broadcast(server, Component.literal(DataManager.nameOf(winner) + " has been elected ")
                .withStyle(ChatFormatting.GOLD)
                .copy().append(Component.literal(office.displayName).withStyle(office.color, ChatFormatting.BOLD))
                .append(Component.literal("!").withStyle(ChatFormatting.GOLD)));
    }

    public static String standings() {
        PoliticsData d = DataManager.data();
        if (!d.officeElectionActive) {
            StringBuilder sb = new StringBuilder("Current office holders:\n");
            for (CivicOffice o : CivicOffice.values()) {
                String h = holder(o);
                sb.append("- ").append(o.displayName).append(": ")
                        .append(h.isEmpty() ? "Vacant" : DataManager.nameOf(h)).append('\n');
            }
            return sb.toString();
        }
        CivicOffice office = CivicOffice.byId(d.officeElectionOffice);
        StringBuilder sb = new StringBuilder("Race for " + (office == null ? "?" : office.displayName) + ":\n");
        List<Map.Entry<String, Integer>> ranked = new ArrayList<>(d.officeVotes.entrySet());
        ranked.sort(Comparator.comparingInt((Map.Entry<String, Integer> e) -> effectiveVotes(e.getKey())).reversed());
        for (var e : ranked) {
            String u = e.getKey();
            sb.append("- ").append(DataManager.nameOf(u)).append(": ").append(effectiveVotes(u))
                    .append(" votes (").append(d.campaignFunds.getOrDefault(u, 0)).append(" campaign coins)\n");
            String m = d.manifestos.get(u);
            if (m != null && !m.isEmpty()) sb.append("    \"").append(m).append("\"\n");
        }
        if (ranked.isEmpty()) sb.append("(no candidates yet)\n");
        return sb.toString();
    }

    private static void broadcast(MinecraftServer server, Component msg) {
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }
}
