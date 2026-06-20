package com.political.bounty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /bounty info|list|start commands. */
public final class BountyCommands {

    private BountyCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bounty")
                .then(Commands.literal("info").executes(BountyCommands::info))
                .then(Commands.literal("list").executes(BountyCommands::list))
                .then(Commands.literal("status").executes(BountyCommands::status))
                .then(Commands.literal("cancel").executes(BountyCommands::cancel))
                .then(Commands.literal("quest")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                        .executes(BountyCommands::quest))))
                .then(Commands.literal("start")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                        .executes(BountyCommands::start)))));
    }

    private static int status(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        c.getSource().sendSuccess(() -> Component.literal(BountyManager.questStatus(p.getStringUUID()))
                .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int cancel(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return BountyManager.cancelQuest(p)
                ? ok(c, "Quest cancelled.") : fail(c, "You have no active quest.");
    }

    private static int quest(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        BountyType type = BountyType.byId(StringArgumentType.getString(c, "type"));
        if (type == null) return fail(c, "Unknown discipline. Use /bounty list.");
        int tier = IntegerArgumentType.getInteger(c, "tier");
        return BountyManager.startQuest(p, type, tier)
                ? 1 : fail(c, "You already have an active quest. Use /bounty status or /bounty cancel.");
    }

    private static int ok(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int fail(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendFailure(Component.literal(msg).withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int info(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        StringBuilder sb = new StringBuilder("=== Bounty Hunt ===\n");
        for (BountyType t : BountyType.values()) {
            sb.append(t.displayName).append(": level ").append(BountyManager.getLevel(p.getStringUUID(), t))
                    .append(" (").append(BountyManager.getXp(p.getStringUUID(), t)).append(" xp)\n");
        }
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GOLD);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("Disciplines: ");
        boolean first = true;
        for (BountyType t : BountyType.values()) {
            if (!first) sb.append(", ");
            sb.append(t.name().toLowerCase()).append(" (").append(t.displayName).append(")");
            first = false;
        }
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        BountyType type = BountyType.byId(StringArgumentType.getString(c, "type"));
        if (type == null) {
            c.getSource().sendFailure(Component.literal("Unknown discipline. Use /bounty list.").withStyle(ChatFormatting.RED));
            return 0;
        }
        int tier = IntegerArgumentType.getInteger(c, "tier");
        if (!BountyManager.startBounty(p, type, tier)) {
            c.getSource().sendFailure(Component.literal("Failed to spawn bounty boss.").withStyle(ChatFormatting.RED));
            return 0;
        }
        return 1;
    }
}
