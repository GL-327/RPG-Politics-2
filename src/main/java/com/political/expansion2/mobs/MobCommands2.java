package com.political.expansion2.mobs;

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

public final class MobCommands2 {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private static final SuggestionProvider<CommandSourceStack> MOB_IDS = (ctx, builder) -> {
        for (String id : ExpansionMobs2.ids()) builder.suggest(id);
        return builder.buildFuture();
    };

    private MobCommands2() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("rpgmob2").requires(OP)
                .then(Commands.literal("list").executes(MobCommands2::list))
                .then(Commands.literal("spawns").executes(MobCommands2::toggleSpawns))
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
        StringBuilder sb = new StringBuilder("RPG creatures phase-2 (").append(ExpansionMobs2.SPECS.size()).append("): ");
        boolean first = true;
        for (MobSpec2 spec : ExpansionMobs2.SPECS) {
            if (!first) sb.append(", ");
            sb.append(spec.id);
            first = false;
        }
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int toggleSpawns(CommandContext<CommandSourceStack> c) {
        boolean now = !ExpansionMobs2.naturalSpawnsEnabled;
        ExpansionMobs2.naturalSpawnsEnabled = now;
        c.getSource().sendSuccess(() -> Component.literal("Phase-2 creature natural spawning "
                + (now ? "enabled" : "disabled") + ".").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int summon(CommandContext<CommandSourceStack> c, int count) throws CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        ServerLevel level = player.level();
        String id = StringArgumentType.getString(c, "id");
        if (ExpansionMobs2.specById(id) == null) {
            c.getSource().sendFailure(Component.literal("Unknown phase-2 creature id: " + id));
            return 0;
        }
        BlockPos base = player.blockPosition();
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            BlockPos at = base.offset(
                    (int) ((Math.random() - 0.5) * 6),
                    0,
                    (int) ((Math.random() - 0.5) * 6));
            if (ExpansionMobs2.spawnById(level, at, id) != null) spawned++;
        }
        int s = spawned;
        c.getSource().sendSuccess(() -> Component.literal("Spawned " + s + " x " + id + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }
}
