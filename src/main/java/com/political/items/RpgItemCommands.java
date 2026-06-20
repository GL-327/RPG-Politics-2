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
    public static int price(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 2;
            case UNCOMMON -> 3;
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
                                .executes(RpgItemCommands::give)))
                .then(Commands.literal("stats").executes(RpgItemCommands::stats))
                .then(Commands.literal("reforge")
                        .then(Commands.literal("rarity")
                                .then(Commands.argument("rarity", StringArgumentType.word())
                                        .executes(RpgItemCommands::reforgeRarity)))
                        .then(Commands.literal("unique").executes(c -> reforgeVariant(c, Variant.UNIQUE, 0)))
                        .then(Commands.literal("none").executes(c -> reforgeVariant(c, Variant.NONE, 0)))
                        .then(Commands.literal("cursed")
                                .then(Commands.argument("grade",
                                        com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 4))
                                        .executes(c -> reforgeVariant(c, Variant.CURSED,
                                                com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "grade")))))));
    }

    /** Reads back the live stat sheet of the held item (works for vanilla and custom). */
    private static int stats(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ItemStack s = p.getMainHandItem();
        if (s.isEmpty()) return fail(c, "Hold an item to inspect.");
        ItemStats.Sheet sheet = ItemStats.compute(s);
        Rarity r = ItemStats.rarityOf(s);
        Variant v = ItemStats.variantOf(s);
        StringBuilder sb = new StringBuilder();
        sb.append(r.display).append(v == Variant.NONE ? "" : " " + v.display).append(" item\n");
        line(sb, "Health", sheet.health); line(sb, "Defense", sheet.defense);
        line(sb, "Strength", sheet.strength); line(sb, "Crit Chance", sheet.critChance);
        line(sb, "Crit Damage", sheet.critDamage); line(sb, "Ferocity", sheet.ferocity);
        line(sb, "Speed", sheet.speed); line(sb, "Mana", sheet.intelligence);
        line(sb, "Cursed Energy", sheet.cursed);
        Component msg = Component.literal(sb.toString()).withStyle(r.color);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static void line(StringBuilder sb, String name, double v) {
        if (v != 0) sb.append("  ").append(name).append(": +").append((int) Math.round(v)).append('\n');
    }

    private static int reforgeRarity(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ItemStack s = p.getMainHandItem();
        if (s.isEmpty()) return fail(c, "Hold an item to reforge.");
        Rarity r = Rarity.byId(StringArgumentType.getString(c, "rarity"));
        if (r == null) return fail(c, "Unknown rarity. Use common/uncommon/rare/epic/legendary/mythic.");
        ItemStats.setRarity(s, r);
        ItemStats.decorate(s);
        return ok(c, "Reforged to " + r.display + ".");
    }

    private static int reforgeVariant(CommandContext<CommandSourceStack> c, Variant variant, int grade)
            throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ItemStack s = p.getMainHandItem();
        if (s.isEmpty()) return fail(c, "Hold an item to reforge.");
        if (variant == Variant.CURSED) ItemStats.setCursedGrade(s, grade);
        else ItemStats.setVariant(s, variant);
        ItemStats.decorate(s);
        return ok(c, "Applied variant: " + (variant == Variant.NONE ? "none" : variant.display)
                + (variant == Variant.CURSED ? " (Grade " + grade + ")" : "") + ".");
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
        if (def.intelligence != 0) sb.append("  Mana +").append(def.intelligence).append('\n');
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
