package com.political.items;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * RPG gear commands. {@code list}/{@code info} form an in-game catalogue (the
 * dependency-free "recipe viewer"), {@code buy} purchases gear for credits, and
 * {@code give} is operator-only.
 */
public final class RpgItemCommands {

    private RpgItemCommands() {}

    /** Credit price per rarity tier. */
    public static int price(RpgItem.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 2;
            case RARE -> 5;
            case EPIC -> 12;
            case LEGENDARY -> 30;
            case MYTHIC -> 75;
        };
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("rpgitem")
                .then(Commands.literal("list").executes(RpgItemCommands::list))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(RpgItemCommands::info)))
                .then(Commands.literal("buy")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(RpgItemCommands::buy)))
                .then(Commands.literal("give")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(RpgItemCommands::give))));
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("=== RPG Gear Catalogue ===\n");
        for (RpgItem item : RpgItem.values()) {
            sb.append(item.id()).append(" - ").append(item.displayName)
                    .append(" [").append(item.rarity.name()).append(", ")
                    .append(price(item.rarity)).append(" credits]\n");
        }
        sb.append("Use /rpgitem info <id> for details or /rpgitem buy <id>.");
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> c) {
        RpgItem def = RpgItem.byId(StringArgumentType.getString(c, "id"));
        if (def == null) return fail(c, "Unknown item. Use /rpgitem list.");
        StringBuilder sb = new StringBuilder();
        sb.append(def.displayName).append(" (").append(def.rarity.name()).append(")\n");
        if (def.health != 0) sb.append("  Health +").append(def.health).append('\n');
        if (def.defense != 0) sb.append("  Defense +").append(def.defense).append('\n');
        if (def.strength != 0) sb.append("  Strength +").append(def.strength).append('\n');
        if (def.intelligence != 0) sb.append("  Energy +").append(def.intelligence).append('\n');
        for (Ability a : def.abilities) {
            sb.append("  \u25C6 ").append(a.displayName).append(": ").append(a.description).append('\n');
        }
        sb.append("  Price: ").append(price(def.rarity)).append(" credits");
        Component msg = Component.literal(sb.toString()).withStyle(def.rarity.color);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int buy(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        RpgItem def = RpgItem.byId(StringArgumentType.getString(c, "id"));
        if (def == null) return fail(c, "Unknown item. Use /rpgitem list.");
        int cost = price(def.rarity);
        if (!DataManager.removeCredits(p.getStringUUID(), cost))
            return fail(c, def.displayName + " costs " + cost + " credits. Earn credits via /convert or bounties.");
        grant(p, def);
        return ok(c, "Purchased " + def.displayName + " for " + cost + " credits.");
    }

    private static int give(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        RpgItem def = RpgItem.byId(StringArgumentType.getString(c, "id"));
        if (def == null) return fail(c, "Unknown item. Use /rpgitem list.");
        grant(p, def);
        return ok(c, "Granted " + def.displayName + ".");
    }

    private static void grant(ServerPlayer p, RpgItem def) {
        ItemStack stack = RpgItems.create(def);
        if (!p.getInventory().add(stack)) {
            p.drop(stack, false);
        }
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
