package com.political.politics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/** Government quality-of-life commands: intercom, relocate, weather control, vanish. */
public final class GovExtrasCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);
    private static final Set<UUID> VANISHED = new HashSet<>();

    private GovExtrasCommands() {}

    private static boolean opOrChair(CommandSourceStack src) {
        if (OP.test(src)) return true;
        ServerPlayer p = src.getPlayer();
        return p != null && DataManager.isChair(p.getUUID());
    }

    private static boolean opOrJudge(CommandSourceStack src) {
        if (OP.test(src)) return true;
        ServerPlayer p = src.getPlayer();
        return p != null && DataManager.isJudge(p.getUUID());
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("intercom").requires(GovExtrasCommands::opOrChair)
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(GovExtrasCommands::intercom)));

        d.register(Commands.literal("relocate").requires(GovExtrasCommands::opOrJudge)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(GovExtrasCommands::relocate)));

        d.register(Commands.literal("weather").requires(GovExtrasCommands::opOrChair)
                .then(Commands.literal("clear").executes(c -> weather(c, false, false)))
                .then(Commands.literal("rain").executes(c -> weather(c, true, false)))
                .then(Commands.literal("thunder").executes(c -> weather(c, true, true))));

        d.register(Commands.literal("vanish").requires(OP).executes(GovExtrasCommands::vanish));
    }

    private static int intercom(CommandContext<CommandSourceStack> c) {
        String message = StringArgumentType.getString(c, "message");
        Component msg = Component.literal("[INTERCOM] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .copy().append(Component.literal(message).withStyle(ChatFormatting.YELLOW));
        c.getSource().getServer().getPlayerList().broadcastSystemMessage(msg, false);
        return 1;
    }

    private static int relocate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        double angle = Math.random() * Math.PI * 2;
        double dist = 100 + Math.random() * 900;
        double x = target.getX() + Math.cos(angle) * dist;
        double z = target.getZ() + Math.sin(angle) * dist;
        target.connection.teleport(x, target.getY(), z, target.getYRot(), target.getXRot());
        target.sendSystemMessage(Component.literal("You have been relocated by the authorities.")
                .withStyle(ChatFormatting.RED));
        c.getSource().sendSuccess(() -> Component.literal("Relocated " + target.getName().getString() + ".")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int weather(CommandContext<CommandSourceStack> c, boolean raining, boolean thunder) {
        var server = c.getSource().getServer();
        String vanilla = !raining ? "weather clear" : (thunder ? "weather thunder" : "weather rain");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), vanilla);
        c.getSource().sendSuccess(() -> Component.literal("Weather updated.").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int vanish(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        UUID id = p.getUUID();
        if (VANISHED.remove(id)) {
            p.removeEffect(MobEffects.INVISIBILITY);
            return msg(c, "You are no longer vanished.");
        }
        VANISHED.add(id);
        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        return msg(c, "You have vanished.");
    }

    private static int msg(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendSuccess(() -> Component.literal(text).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }
}
