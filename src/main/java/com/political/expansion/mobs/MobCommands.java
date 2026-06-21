package com.political.expansion.mobs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/**
 * Operator commands for the RPG creature set. Exposed via {@link #register} so the integration
 * agent can wire it into the shared {@code CommandRegistrationCallback}. Mirrors the structure of
 * {@code com.political.curse.CurseCommands}.
 *
 * <ul>
 *   <li>{@code /rpgmob list} – list every creature id</li>
 *   <li>{@code /rpgmob summon <id> [count]} – spawn near you</li>
 *   <li>{@code /rpgmob boss <id>} – spawn a single creature (intended for bosses)</li>
 *   <li>{@code /rpgmob spawns} – toggle natural spawning of new creatures</li>
 * </ul>
 */
public final class MobCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private static final SuggestionProvider<CommandSourceStack> MOB_IDS = (ctx, builder) -> {
        for (String id : ExpansionMobs.ids()) builder.suggest(id);
        return builder.buildFuture();
    };

    private MobCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("rpgmob").requires(OP)
                .then(Commands.literal("list").executes(MobCommands::list))
                .then(Commands.literal("spawns").executes(MobCommands::toggleSpawns))
                .then(Commands.literal("summon")
                        .then(Commands.argument("id", StringArgumentType.string()).suggests(MOB_IDS)
                                .executes(c -> summon(c, 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                        .executes(c -> summon(c, IntegerArgumentType.getInteger(c, "count"))))))
                .then(Commands.literal("boss")
                        .then(Commands.argument("id", StringArgumentType.string()).suggests(MOB_IDS)
                                .executes(c -> summon(c, 1)))));
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("RPG creatures (").append(ExpansionMobs.SPECS.size()).append("): ");
        boolean first = true;
        for (MobSpec spec : ExpansionMobs.SPECS) {
            if (!first) sb.append(", ");
            sb.append(spec.id);
            first = false;
        }
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int toggleSpawns(CommandContext<CommandSourceStack> c) {
        boolean now = !ExpansionMobs.naturalSpawnsEnabled;
        ExpansionMobs.naturalSpawnsEnabled = now;
        c.getSource().sendSuccess(() -> Component.literal("RPG creature natural spawning "
                + (now ? "enabled" : "disabled") + ".").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int summon(CommandContext<CommandSourceStack> c, int count) throws CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        ServerLevel level = player.level();
        String id = StringArgumentType.getString(c, "id");
        if (ExpansionMobs.specById(id) == null) {
            c.getSource().sendFailure(Component.literal("Unknown creature id: " + id));
            return 0;
        }
        BlockPos base = player.blockPosition();
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            BlockPos at = base.offset(
                    (int) ((Math.random() - 0.5) * 6),
                    0,
                    (int) ((Math.random() - 0.5) * 6));
            if (ExpansionMobs.spawnById(level, at, id) != null) spawned++;
        }
        int s = spawned;
        c.getSource().sendSuccess(() -> Component.literal("Spawned " + s + " x " + id + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }
}
