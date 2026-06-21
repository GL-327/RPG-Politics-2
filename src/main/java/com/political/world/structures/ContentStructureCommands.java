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

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Commands for the content workstream's original surface structures. */
public final class ContentStructureCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private static final SuggestionProvider<CommandSourceStack> KIND_SUGGEST =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(ContentStructureKind.values()).map(k -> k.id).collect(Collectors.toList()),
                    builder);

    private ContentStructureCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("cstructure")
                .then(Commands.literal("list").executes(ContentStructureCommands::list))
                .then(Commands.literal("types").executes(ContentStructureCommands::types))
                .then(Commands.literal("locate")
                        .executes(ContentStructureCommands::locate)
                        .then(Commands.argument("kind", StringArgumentType.word())
                                .suggests(KIND_SUGGEST)
                                .executes(ContentStructureCommands::locateKind)))
                .then(Commands.literal("summon").requires(OP)
                        .then(Commands.argument("kind", StringArgumentType.word())
                                .suggests(KIND_SUGGEST)
                                .executes(ContentStructureCommands::summonHere)
                                .then(Commands.literal("at")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(ContentStructureCommands::summonAt)))))));
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        var sites = ContentStructureManager.sites();
        if (sites.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("No content structures discovered yet.")
                    .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Known content structures (" + sites.size() + "):")
                .withStyle(ChatFormatting.GOLD), false);
        for (ContentStructureManager.ContentSite s : sites) {
            c.getSource().sendSuccess(() -> Component.literal("  " + s.kind().display + " @ "
                    + s.x() + ", " + s.y() + ", " + s.z()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        return sites.size();
    }

    private static int types(CommandContext<CommandSourceStack> c) {
        c.getSource().sendSuccess(() -> Component.literal("Content structure kinds:").withStyle(ChatFormatting.GOLD), false);
        for (ContentStructureKind k : ContentStructureKind.values()) {
            c.getSource().sendSuccess(() -> Component.literal("  " + k.id + " \u2014 " + k.display)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        return ContentStructureKind.values().length;
    }

    private static int locate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        ContentStructureManager.ContentSite s = ContentStructureManager.nearest(level, p.getX(), p.getZ());
        return reportLocate(c, p, s, "content structures");
    }

    private static int locateKind(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        ContentStructureKind kind = requireKind(c);
        if (kind == null) return 0;
        ContentStructureManager.ContentSite s = ContentStructureManager.nearestOfKind(level, p.getX(), p.getZ(), kind);
        return reportLocate(c, p, s, kind.display + "s");
    }

    private static int reportLocate(CommandContext<CommandSourceStack> c, ServerPlayer p,
                                    ContentStructureManager.ContentSite s, String what) {
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No " + what + " found in this dimension yet. Keep exploring!")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        int dx = s.x() - p.getBlockX();
        int dz = s.z() - p.getBlockZ();
        int dist = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
        c.getSource().sendSuccess(() -> Component.literal("Nearest: " + s.kind().display + " (~" + dist + "m)")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        c.getSource().sendSuccess(() -> Component.literal("Coordinates: " + s.x() + ", " + s.y() + ", " + s.z())
                .withStyle(ChatFormatting.GRAY), false);
        return Math.max(1, dist);
    }

    private static int summonHere(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        ContentStructureKind kind = requireKind(c);
        if (kind == null) return 0;
        int x = p.getBlockX() + 12;
        int z = p.getBlockZ() + 12;
        ContentStructureManager.ContentSite site = ContentStructureManager.queueAt(level, x, z, kind);
        c.getSource().sendSuccess(() -> Component.literal("Queued " + kind.display + " at "
                + site.x() + ", " + site.z() + ".").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return 1;
    }

    private static int summonAt(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        ContentStructureKind kind = requireKind(c);
        if (kind == null) return 0;
        int x = IntegerArgumentType.getInteger(c, "x");
        int z = IntegerArgumentType.getInteger(c, "z");
        ContentStructureManager.ContentSite site = ContentStructureManager.queueAt(level, x, z, kind);
        c.getSource().sendSuccess(() -> Component.literal("Queued " + kind.display + " at "
                + site.x() + ", " + site.z() + ".").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return 1;
    }

    private static ContentStructureKind requireKind(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "kind");
        ContentStructureKind kind = ContentStructureKind.byId(id);
        if (kind == null) {
            c.getSource().sendFailure(Component.literal("Unknown content structure kind: " + id));
        }
        return kind;
    }
}
