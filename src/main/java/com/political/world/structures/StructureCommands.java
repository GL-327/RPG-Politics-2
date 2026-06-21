package com.political.world.structures;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/**
 * Player and admin commands for surface-structure discovery and summoning:
 * {@code /structure list|locate|types} for everyone and {@code /structure summon <type> [at x z]}
 * for game-masters. Mirrors the dungeon command surface so both systems feel consistent.
 */
public final class StructureCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private static final SuggestionProvider<CommandSourceStack> TYPE_SUGGEST =
            (ctx, builder) -> SharedSuggestionProvider.suggest(StructureType.ids(), builder);

    private StructureCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("structure")
                .then(Commands.literal("list").executes(StructureCommands::list))
                .then(Commands.literal("types").executes(StructureCommands::types))
                .then(Commands.literal("locate")
                        .executes(StructureCommands::locate)
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(TYPE_SUGGEST)
                                .executes(StructureCommands::locateType)))
                .then(Commands.literal("summon").requires(OP)
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(TYPE_SUGGEST)
                                .executes(StructureCommands::summonHere)
                                .then(Commands.literal("at")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(StructureCommands::summonAt)))))));
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        var sites = StructureManager.sites();
        if (sites.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("No surface structures discovered yet.")
                    .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Known surface structures (" + sites.size() + "):")
                .withStyle(ChatFormatting.GOLD), false);
        for (StructureSite s : sites) {
            c.getSource().sendSuccess(() -> Component.literal("  " + s.type.display + " @ "
                    + s.x + ", " + s.y + ", " + s.z).withStyle(s.type.color), false);
        }
        return sites.size();
    }

    private static int types(CommandContext<CommandSourceStack> c) {
        c.getSource().sendSuccess(() -> Component.literal("Surface structure types:").withStyle(ChatFormatting.GOLD), false);
        for (StructureType t : StructureType.values()) {
            c.getSource().sendSuccess(() -> Component.literal("  " + t.id + " \u2014 " + t.display
                    + " [" + t.faction.name().toLowerCase() + "]").withStyle(t.color), false);
        }
        return StructureType.values().length;
    }

    private static int locate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        StructureSite s = StructureManager.nearest(level, p.getX(), p.getZ());
        return reportLocate(c, p, s, "surface structures");
    }

    private static int locateType(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        StructureType type = requireType(c);
        if (type == null) return 0;
        StructureSite s = StructureManager.nearestOfType(level, p.getX(), p.getZ(), type);
        return reportLocate(c, p, s, type.display + "s");
    }

    private static int reportLocate(CommandContext<CommandSourceStack> c, ServerPlayer p,
                                    StructureSite s, String what) {
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No " + what + " found in this dimension yet. Keep exploring!")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        int dx = s.x - p.getBlockX();
        int dz = s.z - p.getBlockZ();
        int dist = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
        c.getSource().sendSuccess(() -> Component.literal("Nearest: " + s.type.display + " (~" + dist + "m)")
                .withStyle(s.type.color, ChatFormatting.BOLD), false);
        c.getSource().sendSuccess(() -> Component.literal("Coordinates: " + s.x + ", " + s.y + ", " + s.z)
                .withStyle(ChatFormatting.GRAY), false);
        return Math.max(1, dist);
    }

    private static int summonHere(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        StructureType type = requireType(c);
        if (type == null) return 0;
        int x = p.getBlockX() + 10;
        int z = p.getBlockZ() + 10;
        StructureSite site = StructureManager.queueAt(level, x, z, type);
        c.getSource().sendSuccess(() -> Component.literal("Queued " + type.display + " at "
                + site.x + ", " + site.z + ".").withStyle(type.color), true);
        return 1;
    }

    private static int summonAt(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        StructureType type = requireType(c);
        if (type == null) return 0;
        int x = IntegerArgumentType.getInteger(c, "x");
        int z = IntegerArgumentType.getInteger(c, "z");
        StructureSite site = StructureManager.queueAt(level, x, z, type);
        c.getSource().sendSuccess(() -> Component.literal("Queued " + type.display + " at "
                + site.x + ", " + site.z + ".").withStyle(type.color), true);
        return 1;
    }

    private static StructureType requireType(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "type");
        StructureType type = StructureType.byId(id);
        if (type == null) {
            c.getSource().sendFailure(Component.literal("Unknown structure type: " + id));
        }
        return type;
    }
}
