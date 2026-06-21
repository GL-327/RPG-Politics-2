package com.political.expansion2.quests;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.political.expansion2.npc.Expansion2BossSpawner;
import com.political.expansion2.npc.NamedNpcBoss;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Player-facing quest commands for Expansion 2. */
public final class QuestCommands {

    private QuestCommands() {}

    public static void register(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> d) {
        d.register(Commands.literal("exp2quest")
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            var p = ctx.getSource().getPlayer();
                            if (p == null) return 0;
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Registered quests: " + QuestRegistry.count()), false);
                            for (QuestDef q : QuestRegistry.all()) {
                                if (QuestRegistry.all().indexOf(q) >= 15) break;
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Use /exp2quest offers <ARCHETYPE> — e.g. BOUNTY_BROKER"), false);
                            return 1;
                        }))
                .then(Commands.literal("offers")
                        .then(Commands.argument("archetype", StringArgumentType.string())
                                .executes(ctx -> {
                                    var p = ctx.getSource().getPlayer();
                                    if (p == null) return 0;
                                    try {
                                        var arch = com.political.expansion2.npc.NpcArchetype.valueOf(
                                                StringArgumentType.getString(ctx, "archetype").toUpperCase());
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                Expansion2QuestManager.listOffersFor(p, arch)), false);
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown archetype."));
                                        return 0;
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("accept")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    var p = ctx.getSource().getPlayer();
                                    if (p == null) return 0;
                                    String msg = Expansion2QuestManager.accept(p, StringArgumentType.getString(ctx, "id"));
                                    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                                    return 1;
                                })))
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            var p = ctx.getSource().getPlayer();
                            if (p == null) return 0;
                            ctx.getSource().sendSuccess(() -> Component.literal(Expansion2QuestManager.status(p)), false);
                            return 1;
                        }))
                .then(Commands.literal("abandon")
                        .executes(ctx -> {
                            var p = ctx.getSource().getPlayer();
                            if (p == null) return 0;
                            boolean ok = Expansion2QuestManager.abandon(p);
                            ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Quest abandoned." : "Nothing to abandon."), false);
                            return 1;
                        }))
                .then(Commands.literal("boss")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    var p = ctx.getSource().getPlayer();
                                    if (p == null) return 0;
                                    NamedNpcBoss b = NamedNpcBoss.byId(StringArgumentType.getString(ctx, "id"));
                                    if (b == null) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown boss id."));
                                        return 0;
                                    }
                                    Expansion2BossSpawner.spawn(p, b);
                                    return 1;
                                })))
                .then(Commands.literal("give")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("item", StringArgumentType.string())
                                .executes(ctx -> {
                                    var p = ctx.getSource().getPlayer();
                                    if (p == null) return 0;
                                    var item = QuestItems.get(StringArgumentType.getString(ctx, "item"));
                                    if (item == null) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown quest item."));
                                        return 0;
                                    }
                                    p.getInventory().add(new net.minecraft.world.item.ItemStack(item));
                                    return 1;
                                })
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            var p = ctx.getSource().getPlayer();
                                            if (p == null) return 0;
                                            var item = QuestItems.get(StringArgumentType.getString(ctx, "item"));
                                            if (item == null) return 0;
                                            p.getInventory().add(new net.minecraft.world.item.ItemStack(item,
                                                    IntegerArgumentType.getInteger(ctx, "count")));
                                            return 1;
                                        })))));
    }
}
