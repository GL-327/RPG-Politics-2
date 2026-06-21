package com.political.expansion2.npc;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class NpcCommands {

    private NpcCommands() {}

    public static void register(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> d) {
        d.register(Commands.literal("exp2npc").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("boss")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    NamedNpcBoss b = NamedNpcBoss.byId(StringArgumentType.getString(ctx, "id"));
                                    if (b == null) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown boss id."));
                                        return 0;
                                    }
                                    var p = ctx.getSource().getPlayer();
                                    if (p == null) return 0;
                                    Expansion2BossSpawner.spawn(p, b);
                                    return 1;
                                })))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Archetypes: " + NpcArchetype.values().length
                                            + " | Bosses: " + NamedNpcBoss.values().length), false);
                            return 1;
                        })));
    }
}
