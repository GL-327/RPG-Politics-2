package com.political.economy;

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

/** /crypto and /stocks trading commands backed by {@link MarketManager}. */
public final class MarketCommands {

    private MarketCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        register(d, "crypto", MarketManager.Category.CRYPTO);
        register(d, "stocks", MarketManager.Category.STOCK);
    }

    private static void register(CommandDispatcher<CommandSourceStack> d, String name, MarketManager.Category cat) {
        d.register(Commands.literal(name)
                .then(Commands.literal("market").executes(c -> market(c, cat)))
                .then(Commands.literal("portfolio").executes(c -> portfolio(c, cat)))
                .then(Commands.literal("buy")
                        .then(Commands.argument("symbol", StringArgumentType.word())
                                .then(Commands.argument("units", IntegerArgumentType.integer(1))
                                        .executes(c -> buy(c, cat)))))
                .then(Commands.literal("sell")
                        .then(Commands.argument("symbol", StringArgumentType.word())
                                .then(Commands.argument("units", IntegerArgumentType.integer(1))
                                        .executes(c -> sell(c, cat))))));
    }

    private static int market(CommandContext<CommandSourceStack> c, MarketManager.Category cat) {
        MarketManager.ensureSeeded();
        StringBuilder sb = new StringBuilder("=== " + cat + " MARKET ===\n");
        for (var e : MarketManager.names(cat).entrySet()) {
            sb.append(e.getKey()).append(" (").append(e.getValue()).append("): ")
                    .append(String.format("%.2f", MarketManager.price(cat, e.getKey()))).append(" coins\n");
        }
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GOLD);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int portfolio(CommandContext<CommandSourceStack> c, MarketManager.Category cat) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        StringBuilder sb = new StringBuilder("=== Your " + cat + " holdings ===\n");
        boolean any = false;
        for (String sym : MarketManager.names(cat).keySet()) {
            long held = MarketManager.held(cat, p.getStringUUID(), sym);
            if (held <= 0) continue;
            any = true;
            double value = held * MarketManager.price(cat, sym);
            sb.append(sym).append(": ").append(held).append(" units (~")
                    .append(String.format("%.0f", value)).append(" coins)\n");
        }
        if (!any) sb.append("(nothing yet)\n");
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int buy(CommandContext<CommandSourceStack> c, MarketManager.Category cat) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String sym = StringArgumentType.getString(c, "symbol").toUpperCase();
        if (!MarketManager.isValid(cat, sym)) return fail(c, "Unknown symbol. Use /" + name(cat) + " market.");
        long units = IntegerArgumentType.getInteger(c, "units");
        long cost = MarketManager.buy(cat, p.getStringUUID(), sym, units);
        return cost < 0 ? fail(c, "Insufficient coins.")
                : ok(c, "Bought " + units + " " + sym + " for " + cost + " coins.");
    }

    private static int sell(CommandContext<CommandSourceStack> c, MarketManager.Category cat) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String sym = StringArgumentType.getString(c, "symbol").toUpperCase();
        if (!MarketManager.isValid(cat, sym)) return fail(c, "Unknown symbol. Use /" + name(cat) + " market.");
        long units = IntegerArgumentType.getInteger(c, "units");
        long gain = MarketManager.sell(cat, p.getStringUUID(), sym, units);
        return gain < 0 ? fail(c, "You don't hold that many units.")
                : ok(c, "Sold " + units + " " + sym + " for " + gain + " coins.");
    }

    private static String name(MarketManager.Category cat) {
        return cat == MarketManager.Category.CRYPTO ? "crypto" : "stocks";
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
