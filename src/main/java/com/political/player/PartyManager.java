package com.political.player;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lightweight in-memory party system. Parties grant a shared bounty-XP bonus
 * (see {@link com.political.bounty.BountyManager}) and are reset on restart.
 */
public final class PartyManager {

    /** member -> leader */
    private static final Map<UUID, UUID> MEMBER_TO_LEADER = new HashMap<>();
    /** leader -> members (including leader) */
    private static final Map<UUID, Set<UUID>> PARTIES = new HashMap<>();
    /** invitee -> leader who invited */
    private static final Map<UUID, UUID> INVITES = new HashMap<>();

    private PartyManager() {}

    public static boolean isInParty(UUID player) {
        return MEMBER_TO_LEADER.containsKey(player);
    }

    public static UUID leaderOf(UUID player) {
        return MEMBER_TO_LEADER.get(player);
    }

    public static Set<UUID> membersOf(UUID leader) {
        return PARTIES.getOrDefault(leader, Set.of());
    }

    public static boolean create(ServerPlayer leader) {
        UUID id = leader.getUUID();
        if (MEMBER_TO_LEADER.containsKey(id)) return false;
        Set<UUID> members = new HashSet<>();
        members.add(id);
        PARTIES.put(id, members);
        MEMBER_TO_LEADER.put(id, id);
        return true;
    }

    public static boolean invite(ServerPlayer leader, ServerPlayer target) {
        UUID lid = leader.getUUID();
        if (!lid.equals(MEMBER_TO_LEADER.get(lid))) return false; // must be leader
        if (MEMBER_TO_LEADER.containsKey(target.getUUID())) return false;
        INVITES.put(target.getUUID(), lid);
        return true;
    }

    public static boolean accept(ServerPlayer target) {
        UUID tid = target.getUUID();
        UUID leader = INVITES.remove(tid);
        if (leader == null) return false;
        Set<UUID> members = PARTIES.get(leader);
        if (members == null) return false;
        members.add(tid);
        MEMBER_TO_LEADER.put(tid, leader);
        return true;
    }

    public static void leave(ServerPlayer player) {
        UUID id = player.getUUID();
        UUID leader = MEMBER_TO_LEADER.remove(id);
        if (leader == null) return;
        Set<UUID> members = PARTIES.get(leader);
        if (members != null) {
            members.remove(id);
            if (id.equals(leader) || members.size() <= 1) {
                for (UUID m : members) MEMBER_TO_LEADER.remove(m);
                PARTIES.remove(leader);
            }
        }
    }

    /** Bonus multiplier applied to bounty XP for players in a party of 2+. */
    public static double xpMultiplier(UUID player) {
        UUID leader = MEMBER_TO_LEADER.get(player);
        if (leader == null) return 1.0;
        Set<UUID> members = PARTIES.get(leader);
        return (members != null && members.size() >= 2) ? 1.15 : 1.0;
    }
}
