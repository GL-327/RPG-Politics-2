package com.political.expansion2.powers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/** Optional /power2 commands (merge into PowerCommands if preferred). */
public final class Power2Commands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private Power2Commands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("power2")
                .executes(Power2Commands::list)
                .then(Commands.literal("list").executes(Power2Commands::list))
                .then(Commands.literal("learn").then(Commands.argument("id", StringArgumentType.word())
                        .executes(Power2Commands::learn)))
                .then(Commands.literal("grant").requires(OP)
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(Power2Commands::grant))));
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("=== Expansion 2 Powers (").append(Power2.values().length).append(") ===\n");
        for (Power2.Category cat : Power2.Category.values()) {
            sb.append("- ").append(cat.label).append(" -\n");
            for (Power2 p : Power2.ofCategory(cat)) {
                sb.append("  ").append(p.id()).append(" — ").append(p.displayName)
                        .append(" (").append(p.energyCost).append(" energy");
                if (p.minGrade > 0) sb.append(", grade ").append(p.minGrade);
                sb.append(")\n");
            }
        }
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int learn(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        Power2 power = Power2.byId(StringArgumentType.getString(c, "id"));
        if (power == null) return fail(c, "Unknown Expansion 2 power. Use /power2 list.");
        if (power.category == Power2.Category.VILTRUMITE || power.category == Power2.Category.HERO) {
            return fail(c, "Hero/Viltrumite powers are granted via Compound V or /power2 grant.");
        }
        int grade = DataManager.sorcererGrade(p.getStringUUID());
        if (power.minGrade > 0 && grade < power.minGrade) {
            return fail(c, power.displayName + " requires " + DataManager.gradeLabel(power.minGrade) + ".");
        }
        if (!DataManager.grantPower(p.getStringUUID(), power.id())) return fail(c, "You already know that power.");
        return ok(c, "You master " + power.displayName + "!");
    }

    private static int grant(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        Power2 power = Power2.byId(StringArgumentType.getString(c, "id"));
        if (power == null) return fail(c, "Unknown Expansion 2 power.");
        DataManager.grantPower(p.getStringUUID(), power.id());
        return ok(c, "Granted " + power.displayName + ".");
    }

    private static int ok(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int fail(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendFailure(Component.literal(msg).withStyle(ChatFormatting.RED));
        return 0;
    }
}
