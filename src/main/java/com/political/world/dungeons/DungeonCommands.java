package com.political.world.dungeons;

import com.mojang.brigadier.CommandDispatcher;
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

/** Player and admin commands for dungeon discovery and summoning. */
public final class DungeonCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private static final SuggestionProvider<CommandSourceStack> TYPE_SUGGEST =
            (ctx, builder) -> SharedSuggestionProvider.suggest(DungeonType.ids(), builder);

    private DungeonCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("dungeon")
                .then(Commands.literal("list").executes(DungeonCommands::list))
                .then(Commands.literal("locate").executes(DungeonCommands::locate))
                .then(Commands.literal("types").executes(DungeonCommands::types))
                .then(Commands.literal("summon").requires(OP)
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(TYPE_SUGGEST)
                                .executes(DungeonCommands::summonHere)
                                .then(Commands.literal("at")
                                        .then(Commands.argument("x", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                                .then(Commands.argument("z", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                                        .executes(DungeonCommands::summonAt)))))));
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        var sites = DungeonManager.sites();
        if (sites.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("No dungeons registered yet.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Known dungeons (" + sites.size() + "):").withStyle(ChatFormatting.GOLD), false);
        for (DungeonSite s : sites) {
            c.getSource().sendSuccess(() -> Component.literal("  " + s.type.display + " @ "
                    + s.x + ", " + s.y + ", " + s.z).withStyle(s.type.color), false);
        }
        return sites.size();
    }

    private static int locate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        DungeonSite s = DungeonManager.nearest(level, p.getX(), p.getZ());
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No dungeons found in this dimension yet. Keep exploring!")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        int dx = s.x - p.getBlockX();
        int dz = s.z - p.getBlockZ();
        int dist = (int) Math.sqrt(dx * dx + dz * dz);
        c.getSource().sendSuccess(() -> Component.literal("Nearest: " + s.type.display + " (~" + dist + "m)")
                .withStyle(s.type.color, ChatFormatting.BOLD), false);
        c.getSource().sendSuccess(() -> Component.literal("Coordinates: " + s.x + ", " + s.y + ", " + s.z)
                .withStyle(ChatFormatting.GRAY), false);
        return dist;
    }

    private static int types(CommandContext<CommandSourceStack> c) {
        c.getSource().sendSuccess(() -> Component.literal("Dungeon types:").withStyle(ChatFormatting.GOLD), false);
        for (DungeonType t : DungeonType.values()) {
            c.getSource().sendSuccess(() -> Component.literal("  " + t.id + " — " + t.display
                    + " [" + t.tier.name().toLowerCase() + "]").withStyle(t.color), false);
        }
        return DungeonType.values().length;
    }

    private static int summonHere(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        DungeonType type = requireType(c);
        if (type == null) return 0;
        int x = p.getBlockX() + 8;
        int z = p.getBlockZ() + 8;
        DungeonSite site = DungeonManager.queueAt(level, x, z, type);
        c.getSource().sendSuccess(() -> Component.literal("Queued " + type.display + " at "
                + site.x + ", " + site.z + ".").withStyle(type.color), true);
        return 1;
    }

    private static int summonAt(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) return 0;
        DungeonType type = requireType(c);
        if (type == null) return 0;
        int x = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "x");
        int z = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "z");
        DungeonSite site = DungeonManager.queueAt(level, x, z, type);
        c.getSource().sendSuccess(() -> Component.literal("Queued " + type.display + " at "
                + site.x + ", " + site.z + ".").withStyle(type.color), true);
        return 1;
    }

    private static DungeonType requireType(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "type");
        DungeonType type = DungeonType.byId(id);
        if (type == null) {
            c.getSource().sendFailure(Component.literal("Unknown dungeon type: " + id));
        }
        return type;
    }
}
