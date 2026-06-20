package com.political.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Premium currency (credits), coin/credit conversion, and the wealth leaderboard. */
public final class CurrencyCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private CurrencyCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("credits")
                .executes(CurrencyCommands::balance)
                .then(Commands.literal("balance").executes(CurrencyCommands::balance))
                .then(Commands.literal("add").requires(OP)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> adjust(c, 1)))))
                .then(Commands.literal("remove").requires(OP)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> adjust(c, -1))))));

        d.register(Commands.literal("convert")
                .then(Commands.literal("tocredits")
                        .then(Commands.argument("credits", IntegerArgumentType.integer(1))
                                .executes(CurrencyCommands::toCredits)))
                .then(Commands.literal("tocoins")
                        .then(Commands.argument("credits", IntegerArgumentType.integer(1))
                                .executes(CurrencyCommands::toCoins))));

        d.register(Commands.literal("baltop").executes(CurrencyCommands::baltop));
    }

    private static int balance(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return ok(c, "Credits: " + DataManager.getCredits(p.getStringUUID())
                + " (1 credit = " + DataManager.COINS_PER_CREDIT + " coins).");
    }

    private static int adjust(CommandContext<CommandSourceStack> c, int sign) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int amount = IntegerArgumentType.getInteger(c, "amount") * sign;
        if (sign < 0 && !DataManager.removeCredits(target.getStringUUID(), -amount))
            return fail(c, "Player does not have that many credits.");
        if (sign > 0) DataManager.addCredits(target.getStringUUID(), amount);
        return ok(c, "Updated " + target.getName().getString() + "'s credits.");
    }

    private static int toCredits(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int credits = IntegerArgumentType.getInteger(c, "credits");
        int cost = credits * DataManager.COINS_PER_CREDIT;
        if (!DataManager.removeCoins(p.getStringUUID(), cost)) return fail(c, "You need " + cost + " coins for that.");
        DataManager.addCredits(p.getStringUUID(), credits);
        return ok(c, "Converted " + cost + " coins into " + credits + " credits.");
    }

    private static int toCoins(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int credits = IntegerArgumentType.getInteger(c, "credits");
        if (!DataManager.removeCredits(p.getStringUUID(), credits)) return fail(c, "You don't have that many credits.");
        DataManager.addCoins(p.getStringUUID(), credits * DataManager.COINS_PER_CREDIT);
        return ok(c, "Converted " + credits + " credits into " + (credits * DataManager.COINS_PER_CREDIT) + " coins.");
    }

    private static int baltop(CommandContext<CommandSourceStack> c) {
        List<Map.Entry<String, String>> names = new ArrayList<>(DataManager.data().playerNames.entrySet());
        names.sort(Comparator.comparingLong((Map.Entry<String, String> e) -> DataManager.netWorth(e.getKey())).reversed());
        StringBuilder sb = new StringBuilder("=== Wealthiest Citizens ===\n");
        int rank = 1;
        for (Map.Entry<String, String> e : names) {
            if (rank > 10) break;
            sb.append(rank).append(". ").append(e.getValue()).append(" - ")
                    .append(DataManager.netWorth(e.getKey())).append(" coins\n");
            rank++;
        }
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GOLD);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int ok(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int fail(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendFailure(Component.literal(msg).withStyle(ChatFormatting.RED));
        return 0;
    }
}
