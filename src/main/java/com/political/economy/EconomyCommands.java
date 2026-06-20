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

/** /bank, /shop, /auction command surface. */
public final class EconomyCommands {

    private EconomyCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bank")
                .then(Commands.literal("balance").executes(EconomyCommands::bankBalance))
                .then(Commands.literal("deposit")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(EconomyCommands::bankDeposit)))
                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(EconomyCommands::bankWithdraw))));

        d.register(Commands.literal("shop")
                .then(Commands.literal("list").executes(EconomyCommands::shopList))
                .then(Commands.literal("buy")
                        .then(Commands.argument("item", StringArgumentType.word())
                                .then(Commands.argument("qty", IntegerArgumentType.integer(1, 64))
                                        .executes(EconomyCommands::shopBuy))))
                .then(Commands.literal("sell").executes(EconomyCommands::shopSell)));

        d.register(Commands.literal("auction")
                .then(Commands.literal("list")
                        .then(Commands.argument("price", IntegerArgumentType.integer(1))
                                .executes(EconomyCommands::auctionList)))
                .then(Commands.literal("browse").executes(EconomyCommands::auctionBrowse))
                .then(Commands.literal("buy")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                .executes(EconomyCommands::auctionBuy))));
    }

    private static int bankBalance(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return ok(c, "Bank balance: " + BankManager.balance(p.getStringUUID()) + " coins.");
    }

    private static int bankDeposit(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(c, "amount");
        return BankManager.deposit(p.getStringUUID(), amount)
                ? ok(c, "Deposited " + amount + " coins.") : fail(c, "Insufficient coins on hand.");
    }

    private static int bankWithdraw(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(c, "amount");
        return BankManager.withdraw(p.getStringUUID(), amount)
                ? ok(c, "Withdrew " + amount + " coins.") : fail(c, "Insufficient bank balance.");
    }

    private static int shopList(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("Shop (buy / sell):\n");
        for (ShopItem s : ShopItem.values()) {
            sb.append(s.id).append(": ").append(s.buyPrice).append(" / ").append(s.sellPrice).append('\n');
        }
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int shopBuy(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ShopItem item = ShopItem.byId(StringArgumentType.getString(c, "item"));
        if (item == null) return fail(c, "Unknown shop item. Use /shop list.");
        int qty = IntegerArgumentType.getInteger(c, "qty");
        return ShopManager.buy(p, item, qty)
                ? ok(c, "Bought " + qty + "x " + item.id + " for " + (item.buyPrice * qty) + " coins.")
                : fail(c, "Insufficient coins.");
    }

    private static int shopSell(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int earned = ShopManager.sellHeld(p);
        return earned >= 0 ? ok(c, "Sold for " + earned + " coins.")
                : fail(c, "Hold a sellable item from /shop list in your main hand.");
    }

    private static int auctionList(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int price = IntegerArgumentType.getInteger(c, "price");
        int id = AuctionManager.list(p, price);
        return id >= 0 ? ok(c, "Listed your held item as auction #" + id + " for " + price + " coins.")
                : fail(c, "Hold the item you want to sell in your main hand.");
    }

    private static int auctionBrowse(CommandContext<CommandSourceStack> c) {
        var listings = AuctionManager.listings();
        if (listings.isEmpty()) return ok(c, "No active auctions.");
        StringBuilder sb = new StringBuilder("Auctions:\n");
        for (var l : listings) {
            sb.append('#').append(l.id()).append(" ").append(l.stack().getCount()).append("x ")
                    .append(l.stack().getItem().toString()).append(" - ").append(l.price())
                    .append(" coins (").append(l.sellerName()).append(")\n");
        }
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int auctionBuy(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int id = IntegerArgumentType.getInteger(c, "id");
        return switch (AuctionManager.buy(p, id)) {
            case 0 -> ok(c, "Purchased auction #" + id + ".");
            case 2 -> fail(c, "Insufficient coins.");
            case 3 -> fail(c, "You cannot buy your own listing.");
            default -> fail(c, "Auction not found.");
        };
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
