package com.political.curse;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.political.curse.spirits.SpiritSpecies;
import com.political.items.ItemStats;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.function.Predicate;

/** Commands to spawn/manage the Curses faction. */
public final class CurseCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private static final SuggestionProvider<CommandSourceStack> SPECIES_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SpiritSpecies.values()).map(SpiritSpecies::id), builder);

    private CurseCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("curse")
                .then(Commands.literal("item").requires(OP)
                        .then(Commands.argument("grade", IntegerArgumentType.integer(1, 4))
                                .executes(CurseCommands::curseItem)))
                .then(Commands.literal("spawn").requires(OP)
                        .then(Commands.argument("grade", IntegerArgumentType.integer(1, 5))
                                .executes(c -> spawn(c, 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                        .executes(c -> spawn(c, IntegerArgumentType.getInteger(c, "count"))))))
                .then(Commands.literal("summon").requires(OP)
                        .then(Commands.argument("species", StringArgumentType.word()).suggests(SPECIES_SUGGESTIONS)
                                .executes(c -> summon(c, 0, 1))
                                .then(Commands.argument("grade", IntegerArgumentType.integer(1, 5))
                                        .executes(c -> summon(c, IntegerArgumentType.getInteger(c, "grade"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                                .executes(c -> summon(c, IntegerArgumentType.getInteger(c, "grade"),
                                                        IntegerArgumentType.getInteger(c, "count")))))))
                .then(Commands.literal("list").requires(OP).executes(CurseCommands::list))
                .then(Commands.literal("toggle").requires(OP).executes(CurseCommands::toggle))
                .then(Commands.literal("object").requires(OP)
                        .executes(c -> object(c, 30))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 500))
                                .executes(c -> object(c, IntegerArgumentType.getInteger(c, "amount"))))));
    }

    private static int curseItem(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int grade = IntegerArgumentType.getInteger(c, "grade");
        var stack = p.getMainHandItem();
        if (stack.isEmpty()) {
            c.getSource().sendFailure(Component.literal("Hold an item to curse."));
            return 0;
        }
        ItemStats.setCursedGrade(stack, grade);
        ItemStats.decorate(stack);
        stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE);
        c.getSource().sendSuccess(() -> Component.literal("Cursed your held item (Grade " + grade + ").")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
        return 1;
    }

    private static int object(CommandContext<CommandSourceStack> c, int amount) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        var stack = p.getMainHandItem();
        if (stack.isEmpty()) return 0;
        CursedObjects.makeCursed(stack, amount);
        c.getSource().sendSuccess(() -> Component.literal("Steeped your held item in " + amount + " cursed energy.")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
        return 1;
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

    private static int summon(CommandContext<CommandSourceStack> c, int gradeArg, int count) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(c, "species");
        SpiritSpecies species = SpiritSpecies.byId(id);
        if (species == null) {
            c.getSource().sendFailure(Component.literal("Unknown cursed spirit: " + id + " (use /curse list)."));
            return 0;
        }
        int grade = gradeArg > 0 ? gradeArg : species.gradeBand();
        ServerLevel level = p.level();
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            BlockPos at = new BlockPos(
                    (int) (p.getX() + (Math.random() - 0.5) * 6),
                    p.getBlockY(),
                    (int) (p.getZ() + (Math.random() - 0.5) * 6));
            if (CurseManager.spawnSpeciesAt(level, at, grade, species) != null) spawned++;
        }
        int s = spawned;
        int g = grade;
        c.getSource().sendSuccess(() -> Component.literal("Manifested " + s + " " + species.displayName()
                + " (" + CurseManager.gradeLabel(g) + ").").withStyle(ChatFormatting.DARK_PURPLE), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        c.getSource().sendSuccess(() -> Component.literal("Cursed spirits by grade:")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
        for (int band = 1; band <= 5; band++) {
            StringBuilder sb = new StringBuilder(CurseManager.gradeLabel(band)).append(": ");
            boolean first = true;
            for (SpiritSpecies sp : SpiritSpecies.values()) {
                if (sp.gradeBand() != band) continue;
                if (!first) sb.append(", ");
                sb.append(sp.id());
                if (sp.boss()) sb.append("*");
                first = false;
            }
            String line = sb.toString();
            c.getSource().sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.GRAY), false);
        }
        c.getSource().sendSuccess(() -> Component.literal("(* = boss). Use /curse summon <id> [grade] [count].")
                .withStyle(ChatFormatting.DARK_GRAY), false);
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
