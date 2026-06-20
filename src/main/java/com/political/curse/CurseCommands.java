package com.political.curse;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/** Commands to spawn/manage the Curses faction. */
public final class CurseCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private CurseCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("curse")
                .then(Commands.literal("spawn").requires(OP)
                        .then(Commands.argument("grade", IntegerArgumentType.integer(1, 5))
                                .executes(c -> spawn(c, 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                        .executes(c -> spawn(c, IntegerArgumentType.getInteger(c, "count"))))))
                .then(Commands.literal("toggle").requires(OP).executes(CurseCommands::toggle)));
    }

    private static int spawn(CommandContext<CommandSourceStack> c, int count) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int grade = IntegerArgumentType.getInteger(c, "grade");
        int spawned = 0;
        for (int i = 0; i < count; i++) if (CurseManager.spawn(p, grade) != null) spawned++;
        int s = spawned;
        c.getSource().sendSuccess(() -> Component.literal("Manifested " + s + " " + CurseManager.gradeLabel(grade) + "(s).")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
        return 1;
    }

    private static int toggle(CommandContext<CommandSourceStack> c) {
        boolean now = !DataManager.data().curseSpawningEnabled;
        DataManager.data().curseSpawningEnabled = now;
        c.getSource().sendSuccess(() -> Component.literal("Natural curse manifestation " + (now ? "enabled" : "disabled") + ".")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
