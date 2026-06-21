package com.political.civics;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Locale;

/** Founding, joining, and the standing perks of player factions / political parties. */
public final class FactionManager {

    private static final int FOUND_COST = 2000;
    private static final int EFFECT_DURATION = 220;
    private static int tickCounter = 0;

    private FactionManager() {}

    public static Faction factionOf(String uuid) {
        String id = DataManager.data().factionOf.get(uuid);
        return id == null ? null : DataManager.data().factions.get(id);
    }

    public static Faction byId(String id) {
        return DataManager.data().factions.get(id);
    }

    private static String slug(String name) {
        String s = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        return s.isEmpty() ? "faction" : s;
    }

    /** Founds a new faction; costs coins and enrols the founder. */
    public static String found(ServerPlayer player, String name, String tag, FactionIdeology ideology) {
        PoliticsData d = DataManager.data();
        String uuid = player.getStringUUID();
        if (factionOf(uuid) != null) return null; // already in a faction
        String id = slug(name);
        if (d.factions.containsKey(id)) return null; // name taken
        if (!DataManager.removeCoins(uuid, FOUND_COST)) return "INSUFFICIENT";
        Faction f = new Faction(id, name, tag, uuid, ideology.name());
        d.factions.put(id, f);
        d.factionOf.put(uuid, id);
        return id;
    }

    public static boolean join(ServerPlayer player, String factionId) {
        PoliticsData d = DataManager.data();
        String uuid = player.getStringUUID();
        if (factionOf(uuid) != null) return false;
        Faction f = d.factions.get(factionId);
        if (f == null) return false;
        if (!f.members.contains(uuid)) f.members.add(uuid);
        d.factionOf.put(uuid, factionId);
        return true;
    }

    public static boolean leave(ServerPlayer player) {
        PoliticsData d = DataManager.data();
        String uuid = player.getStringUUID();
        Faction f = factionOf(uuid);
        if (f == null) return false;
        f.members.remove(uuid);
        d.factionOf.remove(uuid);
        // Disband empty factions.
        if (f.members.isEmpty()) d.factions.remove(f.id);
        return true;
    }

    /** Contributes coins from a member to their party war-chest. */
    public static boolean donate(ServerPlayer player, int coins) {
        Faction f = factionOf(player.getStringUUID());
        if (f == null) return false;
        if (!DataManager.removeCoins(player.getStringUUID(), coins)) return false;
        f.treasury += coins;
        return true;
    }

    /** Adds influence to the faction of the given player (e.g., on winning an office). */
    public static void addInfluence(String uuid, int amount) {
        Faction f = factionOf(uuid);
        if (f != null) f.influence += amount;
    }

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 100 != 0) return; // every ~5s
        if (DataManager.data().factions.isEmpty()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Faction f = factionOf(player.getStringUUID());
            if (f == null) continue;
            FactionIdeology ideology = FactionIdeology.byId(f.ideology);
            player.addEffect(new MobEffectInstance(ideology.effect, EFFECT_DURATION, 0, true, false));
        }
    }

    public static String list() {
        PoliticsData d = DataManager.data();
        if (d.factions.isEmpty()) return "No factions have been founded.";
        StringBuilder sb = new StringBuilder("=== Factions ===\n");
        for (Faction f : d.factions.values()) {
            FactionIdeology ideology = FactionIdeology.byId(f.ideology);
            sb.append(f.tag.isEmpty() ? "" : f.tag + " ").append(f.name)
                    .append(" [").append(ideology.displayName).append("] - ")
                    .append(f.size()).append(" members, ").append(f.influence).append(" influence\n");
        }
        return sb.toString();
    }

    public static Component info(Faction f) {
        FactionIdeology ideology = FactionIdeology.byId(f.ideology);
        StringBuilder sb = new StringBuilder();
        sb.append(f.name).append(f.tag.isEmpty() ? "" : " " + f.tag).append('\n');
        sb.append("Ideology: ").append(ideology.displayName).append(" - ").append(ideology.description).append('\n');
        if (!f.motto.isEmpty()) sb.append("Motto: \"").append(f.motto).append("\"\n");
        sb.append("Founder: ").append(DataManager.nameOf(f.founder)).append('\n');
        sb.append("Members: ").append(f.size()).append('\n');
        sb.append("War-chest: ").append(f.treasury).append(" coins\n");
        sb.append("Influence: ").append(f.influence).append('\n');
        return Component.literal(sb.toString()).withStyle(ideology.color);
    }
}
