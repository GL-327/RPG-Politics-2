package com.political.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.CivicRank;
import com.political.politics.DataManager;
import com.political.politics.Settlement;
import com.political.politics.SettlementType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/** Player-facing and admin commands for settlements, citizenship and local government. */
public final class SettlementCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private SettlementCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("settlement")
                .then(Commands.literal("list").executes(SettlementCommands::list))
                .then(Commands.literal("here").executes(SettlementCommands::here))
                .then(Commands.literal("rank").executes(SettlementCommands::rank))
                .then(Commands.literal("advance").executes(SettlementCommands::advance))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> info(c, StringArgumentType.getString(c, "id")))))
                .then(Commands.literal("vote")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("candidate", EntityArgument.player())
                                        .executes(SettlementCommands::vote))))
                .then(Commands.literal("promote").requires(OP.or(s -> isAnyLeader(s)))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(SettlementCommands::promote)))
                .then(Commands.literal("election").requires(OP)
                        .then(Commands.literal("start")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(SettlementCommands::electionStart))))
                .then(Commands.literal("build").requires(OP)
                        .then(Commands.literal("capital").executes(c -> build(c, SettlementType.CAPITAL)))
                        .then(Commands.literal("city").executes(c -> build(c, SettlementType.CITY)))
                        .then(Commands.literal("town").executes(c -> build(c, SettlementType.TOWN)))
                        .then(Commands.literal("village").executes(c -> build(c, SettlementType.VILLAGE)))));
    }

    private static boolean isAnyLeader(CommandSourceStack s) {
        try {
            return DataManager.isSettlementLeader(s.getPlayerOrException().getStringUUID());
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        var all = DataManager.settlements().values();
        if (all.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("No settlements exist yet.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Settlements (" + all.size() + "):").withStyle(ChatFormatting.GOLD), false);
        for (Settlement s : all) {
            String ruler = s.governedBy.isEmpty() ? "sovereign"
                    : ("under " + nameOrId(s.governedBy));
            c.getSource().sendSuccess(() -> Component.literal(" \u2022 " + s.name + " [" + s.type.display + "] @"
                    + s.x + "," + s.z + " (" + ruler + ", leader: "
                    + (s.leader.isEmpty() ? "vacant" : DataManager.nameOf(s.leader)) + ")").withStyle(s.type.color), false);
        }
        return 1;
    }

    private static String nameOrId(String id) {
        Settlement s = DataManager.settlement(id);
        return s == null ? id : s.name;
    }

    private static int info(CommandContext<CommandSourceStack> c, String id) {
        Settlement s = DataManager.settlement(id);
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No settlement with id '" + id + "'."));
            return 0;
        }
        sendInfo(c.getSource(), s);
        return 1;
    }

    private static int here(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String home = DataManager.citizenshipOf(p.getStringUUID());
        if (home != null && DataManager.settlement(home) != null) {
            c.getSource().sendSuccess(() -> Component.literal("Home settlement:").withStyle(ChatFormatting.GOLD), false);
            sendInfo(c.getSource(), DataManager.settlement(home));
        }
        if (p.level() instanceof ServerLevel level) {
            Settlement near = SettlementManager.nearestSettlement(level, p.getX(), p.getZ());
            if (near != null) {
                c.getSource().sendSuccess(() -> Component.literal("Nearest settlement: " + near.name + " ("
                        + (int) Math.sqrt(near.distSq(p.getBlockX(), p.getBlockZ())) + " blocks).").withStyle(near.type.color), false);
            }
        }
        return 1;
    }

    private static void sendInfo(CommandSourceStack src, Settlement s) {
        src.sendSuccess(() -> Component.literal(s.name + " \u2014 " + s.type.display).withStyle(s.type.color, ChatFormatting.BOLD), false);
        src.sendSuccess(() -> Component.literal("  Centre: " + s.x + ", " + s.y + ", " + s.z).withStyle(ChatFormatting.GRAY), false);
        src.sendSuccess(() -> Component.literal("  Governance: "
                + (s.governedBy.isEmpty() ? "Sovereign" : "Under " + nameOrId(s.governedBy))).withStyle(ChatFormatting.GRAY), false);
        src.sendSuccess(() -> Component.literal("  Leader: " + (s.leader.isEmpty() ? "vacant" : DataManager.nameOf(s.leader))
                + (s.electionActive ? "  (election in progress)" : "")).withStyle(ChatFormatting.GRAY), false);
    }

    private static int rank(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        CivicRank r = DataManager.civicRank(p.getStringUUID());
        String home = DataManager.citizenshipOf(p.getStringUUID());
        c.getSource().sendSuccess(() -> Component.literal("Civic rank: " + r.display
                + (home == null ? " (no settlement)" : " of " + nameOrId(home))
                + (DataManager.canStandForElection(p.getStringUUID()) ? "  \u2014 eligible to stand for Leader." : ""))
                .withStyle(r.color), false);
        return 1;
    }

    private static int advance(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String uuid = p.getStringUUID();
        if (DataManager.citizenshipOf(uuid) == null) {
            c.getSource().sendFailure(Component.literal("You must be a citizen of a settlement first."));
            return 0;
        }
        CivicRank cur = DataManager.civicRank(uuid);
        if (cur.ordinal() >= CivicRank.COUNCILOR.ordinal()) {
            c.getSource().sendFailure(Component.literal("You are already a Councilor \u2014 the top climbable rank."));
            return 0;
        }
        int cost = cur == CivicRank.CITIZEN ? 500 : 2000;
        if (!DataManager.removeCoins(uuid, cost)) {
            c.getSource().sendFailure(Component.literal("Advancing to the next rank costs " + cost + " coins."));
            return 0;
        }
        CivicRank now = DataManager.promote(uuid);
        c.getSource().sendSuccess(() -> Component.literal("You rose to " + now.display + ". (-" + cost + " coins)")
                .withStyle(now.color), false);
        return 1;
    }

    private static int promote(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "target");
        CivicRank now = DataManager.promote(target.getStringUUID());
        c.getSource().sendSuccess(() -> Component.literal("Promoted " + target.getName().getString() + " to " + now.display + ".")
                .withStyle(now.color), false);
        target.sendSystemMessage(Component.literal("You have been promoted to " + now.display + ".").withStyle(now.color));
        return 1;
    }

    private static int vote(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer voter = c.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(c, "id");
        ServerPlayer cand = EntityArgument.getPlayer(c, "candidate");
        Settlement s = DataManager.settlement(id);
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No settlement with id '" + id + "'."));
            return 0;
        }
        if (SettlementManager.castVote(s, voter, cand.getStringUUID())) {
            c.getSource().sendSuccess(() -> Component.literal("Vote cast for " + cand.getName().getString() + " in " + s.name + ".")
                    .withStyle(ChatFormatting.GREEN), false);
            return 1;
        }
        c.getSource().sendFailure(Component.literal("Vote rejected (no active election, not a citizen, already voted, or invalid candidate)."));
        return 0;
    }

    private static int electionStart(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "id");
        Settlement s = DataManager.settlement(id);
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No settlement with id '" + id + "'."));
            return 0;
        }
        if (SettlementManager.startElection(c.getSource().getServer(), s)) return 1;
        c.getSource().sendFailure(Component.literal("No eligible Councilor candidates in " + s.name + " yet."));
        return 0;
    }

    private static int build(CommandContext<CommandSourceStack> c, SettlementType type) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        String name = SettlementGenerator.pickName(new java.util.Random());
        Settlement s = SettlementGenerator.generate(level, p.getBlockX(), p.getBlockZ(), type, name);
        c.getSource().sendSuccess(() -> Component.literal("Built " + type.display + " '" + name + "' at your location.")
                .withStyle(type.color), false);
        return 1;
    }
}
