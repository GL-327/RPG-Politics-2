package com.political.guide;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /guide} \u2014 open the Field Manual encyclopedia directly. */
public final class GuideCommands {

    private GuideCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("guide")
                .executes(c -> open(c, 0))
                .then(Commands.argument("chapter", IntegerArgumentType.integer(1, 99))
                        .executes(c -> open(c, IntegerArgumentType.getInteger(c, "chapter") - 1))));
    }

    private static int open(CommandContext<CommandSourceStack> c, int chapter) throws CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        GuideItem.open(player, Math.max(0, chapter));
        c.getSource().sendSuccess(() -> Component.literal("Opening the Field Manual\u2026")
                .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }
}
