package com.political.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Commands for the unified power system: powers, Compound V serums, and cursed techniques. */
public final class PowerCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    /** Minimum sorcerer grade required to learn each cursed technique. */
    private static final Map<Power, Integer> TECHNIQUE_GRADE = new EnumMap<>(Power.class);
    static {
        TECHNIQUE_GRADE.put(Power.DIVERGENT_FIST, 1);
        TECHNIQUE_GRADE.put(Power.DISMANTLE, 1);
        TECHNIQUE_GRADE.put(Power.CURSED_SPEECH, 1);
        TECHNIQUE_GRADE.put(Power.CLEAVE, 2);
        TECHNIQUE_GRADE.put(Power.LIMITLESS_BLUE, 2);
        TECHNIQUE_GRADE.put(Power.BLACK_FLASH, 2);
        TECHNIQUE_GRADE.put(Power.LIMITLESS_RED, 3);
        TECHNIQUE_GRADE.put(Power.REVERSE_CURSED, 3);
        TECHNIQUE_GRADE.put(Power.TEN_SHADOWS, 3);
        TECHNIQUE_GRADE.put(Power.SIMPLE_DOMAIN, 2);
        TECHNIQUE_GRADE.put(Power.WORLD_CUTTING_SLASH, 3);
        TECHNIQUE_GRADE.put(Power.HOLLOW_PURPLE, 4);
        TECHNIQUE_GRADE.put(Power.INFINITY, 4);
        TECHNIQUE_GRADE.put(Power.DOMAIN_EXPANSION, 5);
        TECHNIQUE_GRADE.put(Power.SIX_EYES, 5);
    }

    private PowerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("power")
                .executes(PowerCommands::help)
                .then(Commands.literal("list").executes(PowerCommands::list))
                .then(Commands.literal("known").executes(PowerCommands::known))
                .then(Commands.literal("use").executes(PowerCommands::use))
                .then(Commands.literal("info").then(Commands.argument("id", StringArgumentType.word())
                        .executes(PowerCommands::info)))
                .then(Commands.literal("select").then(Commands.argument("id", StringArgumentType.word())
                        .executes(PowerCommands::select)))
                .then(Commands.literal("grant").requires(OP)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(PowerCommands::grant))))
                .then(Commands.literal("revoke").requires(OP)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PowerCommands::revoke))));

        // Compound V serums: drink directly, or give yourself the item to use later.
        d.register(Commands.literal("v")
                .then(Commands.literal("compound").executes(c -> { Serums.drinkCompoundV(self(c)); return 1; }))
                .then(Commands.literal("temp").executes(c -> { Serums.drinkTempV(self(c)); return 1; }))
                .then(Commands.literal("v1").executes(c -> { Serums.drinkV1(self(c)); return 1; }))
                .then(Commands.literal("anti").executes(c -> { Serums.drinkAntiV(self(c)); return 1; }))
                .then(Commands.literal("give").requires(OP)
                        .then(Commands.argument("type", StringArgumentType.word())
                                .executes(PowerCommands::giveSerum))));

        // Jujutsu sorcery.
        d.register(Commands.literal("cursed")
                .then(Commands.literal("info").executes(PowerCommands::cursedInfo))
                .then(Commands.literal("awaken").executes(PowerCommands::awaken))
                .then(Commands.literal("learn").then(Commands.argument("id", StringArgumentType.word())
                        .executes(PowerCommands::learn)))
                .then(Commands.literal("grade").requires(OP)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("grade", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 5))
                                        .executes(PowerCommands::setGrade)))));
    }

    private static ServerPlayer self(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        return c.getSource().getPlayerOrException();
    }

    // ---------------- /power ----------------

    private static int help(CommandContext<CommandSourceStack> c) {
        return ok(c, "Powers: /power list | known | select <id> | use | info <id>. Serums: /v. Sorcery: /cursed.");
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("=== Powers ===\n");
        sb.append("- Compound V -\n");
        for (Power p : Power.ofOrigin(Power.Origin.COMPOUND_V)) appendPower(sb, p);
        sb.append("- Cursed Techniques -\n");
        for (Power p : Power.ofOrigin(Power.Origin.CURSED_TECHNIQUE)) appendPower(sb, p);
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static void appendPower(StringBuilder sb, Power p) {
        sb.append("  ").append(p.id()).append(" - ").append(p.displayName)
                .append(" (").append(p.energyCost).append(" energy)\n");
    }

    private static int known(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = self(c);
        List<String> powers = DataManager.knownPowers(p.getStringUUID());
        if (powers.isEmpty()) return ok(c, "You have no powers yet. Drink Compound V (/v) or awaken sorcery (/cursed awaken).");
        String sel = DataManager.selectedPower(p.getStringUUID());
        StringBuilder sb = new StringBuilder("Your powers (selected marked *):\n");
        for (String id : powers) {
            Power pw = Power.byId(id);
            if (pw == null) continue;
            sb.append(id.equals(sel) ? " * " : "   ").append(pw.displayName).append('\n');
        }
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> c) {
        Power p = Power.byId(StringArgumentType.getString(c, "id"));
        if (p == null) return fail(c, "Unknown power. Use /power list.");
        String text = p.displayName + " [" + p.origin.label + "]\n"
                + "  " + p.description + "\n"
                + "  Energy: " + p.energyCost + "  Cooldown: " + (p.cooldownTicks / 20) + "s";
        c.getSource().sendSuccess(() -> Component.literal(text).withStyle(p.origin.color), false);
        return 1;
    }

    private static int select(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = self(c);
        Power power = Power.byId(StringArgumentType.getString(c, "id"));
        if (power == null) return fail(c, "Unknown power. Use /power list.");
        if (!DataManager.hasPower(p.getStringUUID(), power.id())) return fail(c, "You have not awakened that power.");
        DataManager.setSelectedPower(p.getStringUUID(), power.id());
        return ok(c, "Selected " + power.displayName + ". Activate with the power key or /power use.");
    }

    private static int use(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = self(c);
        Component result = PowerManager.activateSelected(p);
        c.getSource().sendSuccess(() -> result, false);
        return 1;
    }

    private static int grant(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        Power power = Power.byId(StringArgumentType.getString(c, "id"));
        if (power == null) return fail(c, "Unknown power. Use /power list.");
        DataManager.grantPower(target.getStringUUID(), power.id());
        return ok(c, "Granted " + power.displayName + " to " + target.getName().getString() + ".");
    }

    private static int revoke(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        DataManager.revokeAllPowers(target.getStringUUID());
        return ok(c, "Revoked all powers from " + target.getName().getString() + ".");
    }

    // ---------------- /cursed ----------------

    private static int cursedInfo(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = self(c);
        String uuid = p.getStringUUID();
        int grade = DataManager.sorcererGrade(uuid);
        int exorcised = DataManager.data().cursesExorcised.getOrDefault(uuid, 0);
        var trait = DataManager.cursedTrait(uuid);
        return ok(c, "Aptitude: " + trait.display + " | " + DataManager.gradeLabel(grade)
                + " | Curses exorcised: " + exorcised
                + (trait.canUseTechniques() ? " | Learn techniques with /cursed learn <id>."
                        : " | You channel no cursed energy, but cursed tools answer to you."));
    }

    private static int awaken(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = self(c);
        String uuid = p.getStringUUID();
        var trait = DataManager.cursedTrait(uuid);
        if (!trait.canUseTechniques())
            return fail(c, "Your body holds no cursed energy (" + trait.display + "). You cannot channel techniques, but you wield cursed tools with ease.");
        if (DataManager.sorcererGrade(uuid) > 0) return fail(c, "You have already awakened your cursed energy.");
        DataManager.setSorcererGrade(uuid, 1);
        // Learn a random starter technique.
        Power[] starters = {Power.DIVERGENT_FIST, Power.DISMANTLE, Power.CURSED_SPEECH};
        Power starter = starters[(int) (Math.random() * starters.length)];
        DataManager.grantPower(uuid, starter.id());
        return ok(c, "Your cursed energy awakens! You are now a " + DataManager.gradeLabel(1)
                + " and learn " + starter.displayName + ".");
    }

    private static int learn(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = self(c);
        String uuid = p.getStringUUID();
        Power power = Power.byId(StringArgumentType.getString(c, "id"));
        if (power == null || power.origin != Power.Origin.CURSED_TECHNIQUE)
            return fail(c, "That is not a cursed technique. See /power list.");
        int grade = DataManager.sorcererGrade(uuid);
        if (grade < 1) return fail(c, "Awaken your cursed energy first with /cursed awaken.");
        int needed = TECHNIQUE_GRADE.getOrDefault(power, 1);
        if (grade < needed) return fail(c, power.displayName + " requires " + DataManager.gradeLabel(needed)
                + ". Exorcise more curses to grow stronger.");
        if (!DataManager.grantPower(uuid, power.id())) return fail(c, "You already know that technique.");
        return ok(c, "You master " + power.displayName + "!");
    }

    private static int giveSerum(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = self(c);
        String type = StringArgumentType.getString(c, "type");
        if (!ModItems.give(p, type)) return fail(c, "Unknown serum. Use compound | temp | v1 | anti.");
        return ok(c, "Gave you a " + type + " serum. Right-click to drink it.");
    }

    private static int setGrade(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int grade = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "grade");
        DataManager.setSorcererGrade(target.getStringUUID(), grade);
        return ok(c, target.getName().getString() + " is now " + DataManager.gradeLabel(grade) + ".");
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
