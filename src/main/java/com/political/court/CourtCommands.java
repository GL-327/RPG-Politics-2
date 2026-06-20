package com.political.court;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.DataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/** /court start|adjourn and /gavel commands. Available to the elected Judge (or operators). */
public final class CourtCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private CourtCommands() {}

    private static boolean opOrJudge(CommandSourceStack src) {
        if (OP.test(src)) return true;
        ServerPlayer p = src.getPlayer();
        return p != null && DataManager.isJudge(p.getUUID());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("court")
                .requires(CourtCommands::opOrJudge)
                .then(Commands.literal("start")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(CourtCommands::startCourt)))
                .then(Commands.literal("adjourn")
                        .executes(CourtCommands::adjournCourt)));

        dispatcher.register(Commands.literal("gavel")
                .requires(CourtCommands::opOrJudge)
                .executes(CourtCommands::giveGavel));
    }

    private static int startCourt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer judge = ctx.getSource().getPlayerOrException();
        ServerPlayer accused = EntityArgument.getPlayer(ctx, "player");
        return CourtDomainManager.start(judge, accused) ? 1 : 0;
    }

    private static int adjournCourt(CommandContext<CommandSourceStack> ctx) {
        if (!CourtDomainManager.isActive()) {
            ctx.getSource().sendFailure(Component.literal("There is no court in session."));
            return 0;
        }
        CourtDomainManager.adjourn("\u2696 The court has been adjourned by order of the bench.");
        return 1;
    }

    private static int giveGavel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer judge = ctx.getSource().getPlayerOrException();
        if (!judge.getInventory().add(CourtItems.createGavel())) {
            judge.drop(CourtItems.createGavel(), false);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("You have been granted The Gavel."), false);
        return 1;
    }
}
