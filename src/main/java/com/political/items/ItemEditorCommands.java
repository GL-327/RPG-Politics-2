package com.political.items;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers {@code /sbs} (alias {@code /skyblockstats}) — a gamemaster-gated admin/creative tool
 * that opens the {@link com.political.client.SbsScreen Skyblock-stats editor} for the held item.
 */
public final class ItemEditorCommands {

    private ItemEditorCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("sbs")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ItemEditorCommands::open));
        d.register(Commands.literal("skyblockstats")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ItemEditorCommands::open));
    }

    private static int open(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        ItemEditor.sendMenu(player);
        return 1;
    }
}
