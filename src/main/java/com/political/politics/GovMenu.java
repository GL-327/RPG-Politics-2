package com.political.politics;

import com.political.net.GovMenuS2C;
import com.political.net.ModNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Server-side builder + action handler for the Governance GUI. */
public final class GovMenu {

    private GovMenu() {}

    /** Builds and sends a fresh Governance menu snapshot to the player's client. */
    public static void sendMenu(ServerPlayer player) {
        PoliticsData d = DataManager.data();
        String uuid = player.getStringUUID();

        String election;
        StringBuilder candidates = new StringBuilder();
        if (d.electionActive) {
            election = "In progress";
            for (String c : d.candidates) {
                if (candidates.length() > 0) candidates.append(';');
                candidates.append(c).append('|')
                        .append(DataManager.nameOf(c))
                        .append(" (").append(d.votes.getOrDefault(c, 0)).append(')');
            }
        } else {
            election = d.electionSystemEnabled ? "Scheduled" : "Disabled";
        }

        String tax = d.taxEnabled ? (d.taxPercent + "% daily") : "Disabled";

        String cityId = DataManager.citizenshipOf(uuid);
        Settlement city = DataManager.settlement(cityId);
        String citizenship = city != null ? city.name : "None";
        String rank = DataManager.civicRank(uuid).display;

        ModNetworking.send(player, new GovMenuS2C(
                nameOrNone(d.chair), nameOrNone(d.viceChair), nameOrNone(d.judge),
                d.dictatorActive ? DataManager.nameOf(d.dictator) : "none",
                election, tax, citizenship, rank,
                PerkManager.describeActive().getString().trim(),
                d.treasury, DataManager.getCoins(uuid), TaxManager.taxOwed(uuid),
                candidates.toString()));
    }

    /** Handles a {@link com.political.net.GovActionC2S} from the GUI, then resends the menu. */
    public static void handleAction(ServerPlayer player, String action, String arg) {
        switch (action) {
            case "paytax" -> {
                int owed = TaxManager.taxOwed(player.getStringUUID());
                if (owed <= 0) {
                    msg(player, "You owe no tax.", ChatFormatting.GRAY);
                } else if (TaxManager.payTax(player, owed)) {
                    msg(player, "Paid " + owed + " coins in tax.", ChatFormatting.GREEN);
                } else {
                    msg(player, "You don't have enough coins to pay your tax.", ChatFormatting.RED);
                }
            }
            case "vote" -> {
                if (ElectionManager.castVote(player, arg)) {
                    msg(player, "Your vote has been recorded.", ChatFormatting.GREEN);
                } else {
                    msg(player, "Your vote could not be recorded.", ChatFormatting.RED);
                }
            }
            // --- Expansion 3 civics actions (additive; safe no-ops if unused by the client) ---
            case "payfine" -> {
                int paid = com.political.civics.JusticeManager.payFine(player);
                msg(player, paid > 0 ? "Paid " + paid + " coins toward your fine." : "No fine to pay.",
                        paid > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            }
            case "paybail" -> {
                boolean freed = com.political.civics.JusticeManager.payBail(player);
                msg(player, freed ? "Bail paid — you are free." : "Couldn't pay bail.",
                        freed ? ChatFormatting.GREEN : ChatFormatting.RED);
            }
            case "jobwork" -> {
                long r = com.political.civics.JobManager.work(player);
                if (r == -2) msg(player, "Get a job first with /job join <id>.", ChatFormatting.RED);
                else if (r >= 0) msg(player, "You're tired. Try again in " + r + "s.", ChatFormatting.GRAY);
            }
            case "officevote" -> {
                boolean ok = com.political.civics.OfficeManager.vote(player, arg);
                msg(player, ok ? "Vote cast." : "Vote could not be recorded.",
                        ok ? ChatFormatting.GREEN : ChatFormatting.RED);
            }
            case "enactlaw" -> {
                if (!DataManager.isChair(player.getUUID())) { msg(player, "Only the Chair may enact laws.", ChatFormatting.RED); break; }
                com.political.civics.CivicLaw law = com.political.civics.CivicLaw.byId(arg);
                if (law == null) { msg(player, "Unknown law.", ChatFormatting.RED); break; }
                boolean ok = com.political.civics.LawManager.enact(player.level().getServer(), law);
                msg(player, ok ? law.displayName + " enacted." : "Could not enact (already active or treasury short).",
                        ok ? ChatFormatting.GREEN : ChatFormatting.RED);
            }
            default -> { }
        }
        sendMenu(player);
    }

    private static String nameOrNone(String uuid) {
        return uuid == null || uuid.isEmpty() ? "Vacant" : DataManager.nameOf(uuid);
    }

    private static void msg(ServerPlayer player, String text, ChatFormatting color) {
        player.sendSystemMessage(Component.literal(text).withStyle(color), true);
    }
}
