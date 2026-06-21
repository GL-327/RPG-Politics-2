package com.political.politics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** All command-driven politics: voting, roles, perks, justice, tax, treasury, dictatorship. */
public final class PoliticsCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private PoliticsCommands() {}

    private static boolean opOrJudge(CommandSourceStack src) {
        if (OP.test(src)) return true;
        ServerPlayer p = src.getPlayer();
        return p != null && DataManager.isJudge(p.getUUID());
    }

    private static boolean opOrChair(CommandSourceStack src) {
        if (OP.test(src)) return true;
        ServerPlayer p = src.getPlayer();
        return p != null && DataManager.isChair(p.getUUID());
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        // --- Citizen ---
        d.register(Commands.literal("vote")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(PoliticsCommands::vote)));

        d.register(Commands.literal("gov").executes(PoliticsCommands::gov));
        // Opens the Governance GUI.
        d.register(Commands.literal("politics")
                .executes(c -> { GovMenu.sendMenu(c.getSource().getPlayerOrException()); return 1; }));
        d.register(Commands.literal("coins").executes(PoliticsCommands::coins));
        d.register(Commands.literal("pay")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(PoliticsCommands::pay))));

        d.register(Commands.literal("tax")
                .then(Commands.literal("status").executes(PoliticsCommands::taxStatus))
                .then(Commands.literal("pay")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(PoliticsCommands::taxPay)))
                .then(Commands.literal("enable").requires(OP).executes(c -> setTax(c, true)))
                .then(Commands.literal("disable").requires(OP).executes(c -> setTax(c, false)))
                .then(Commands.literal("collect").requires(OP).executes(PoliticsCommands::taxCollect))
                .then(Commands.literal("percent").requires(OP)
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(PoliticsCommands::taxPercent)))
                .then(Commands.literal("tiers").requires(OP)
                        .then(Commands.literal("enable").executes(c -> setTaxTiers(c, true)))
                        .then(Commands.literal("disable").executes(c -> setTaxTiers(c, false)))));

        d.register(Commands.literal("treasury")
                .executes(PoliticsCommands::treasury)
                .then(Commands.literal("give").requires(PoliticsCommands::opOrChair)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(PoliticsCommands::treasuryGive)))));

        // --- Elections / impeachment ---
        d.register(Commands.literal("election").requires(OP)
                .then(Commands.literal("enable").executes(c -> { DataManager.data().electionSystemEnabled = true; return ok(c, "Election scheduler enabled."); }))
                .then(Commands.literal("disable").executes(c -> { DataManager.data().electionSystemEnabled = false; return ok(c, "Election scheduler disabled."); }))
                .then(Commands.literal("start").executes(c -> { ElectionManager.forceStartElection(c.getSource().getServer()); return ok(c, "Election started."); }))
                .then(Commands.literal("end").executes(c -> { ElectionManager.endElection(c.getSource().getServer()); return ok(c, "Election ended."); })));
        d.register(Commands.literal("forceelection").requires(OP)
                .executes(c -> { ElectionManager.forceStartElection(c.getSource().getServer()); return ok(c, "Election started."); }));

        d.register(Commands.literal("impeach")
                .then(Commands.literal("start").requires(PoliticsCommands::opOrJudge)
                        .executes(c -> { ElectionManager.startImpeachment(c.getSource().getServer()); return 1; }))
                .then(Commands.literal("vote")
                        .then(Commands.literal("yes").executes(c -> impeachVote(c, true)))
                        .then(Commands.literal("no").executes(c -> impeachVote(c, false)))));

        // --- Roles (OP) ---
        d.register(Commands.literal("role").requires(OP)
                .then(Commands.literal("chair").then(Commands.argument("player", EntityArgument.player())
                        .executes(c -> setRole(c, Role.CHAIR))))
                .then(Commands.literal("vicechair").then(Commands.argument("player", EntityArgument.player())
                        .executes(c -> setRole(c, Role.VICE_CHAIR))))
                .then(Commands.literal("judge").then(Commands.argument("player", EntityArgument.player())
                        .executes(c -> setRole(c, Role.JUDGE)))));

        // --- Perks (Chair / OP) ---
        d.register(Commands.literal("perk")
                .then(Commands.literal("list").executes(PoliticsCommands::perkList))
                .then(Commands.literal("active").executes(c -> ok(c, PerkManager.describeActive())))
                .then(Commands.literal("chair").requires(PoliticsCommands::opOrChair)
                        .then(Commands.argument("perks", StringArgumentType.greedyString())
                                .executes(c -> selectPerks(c, true))))
                .then(Commands.literal("vice").requires(PoliticsCommands::opOrChair)
                        .then(Commands.argument("perks", StringArgumentType.greedyString())
                                .executes(c -> selectPerks(c, false))))
                .then(Commands.literal("clear").requires(OP)
                        .executes(c -> { PerkManager.clearAllPerks(); return ok(c, "All perks cleared."); })));

        // --- Justice (Judge / OP) ---
        d.register(Commands.literal("imprison").requires(PoliticsCommands::opOrJudge)
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("minutes", IntegerArgumentType.integer(1, 1440))
                                .executes(PoliticsCommands::imprisonHere)
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(PoliticsCommands::imprisonAt))))));
        d.register(Commands.literal("pardon").requires(PoliticsCommands::opOrJudge)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(PoliticsCommands::pardon)));
        d.register(Commands.literal("exile").requires(PoliticsCommands::opOrJudge)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(PoliticsCommands::exile)));
        d.register(Commands.literal("smite").requires(PoliticsCommands::opOrJudge)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(PoliticsCommands::smite)));

        // --- Dictatorship (OP) ---
        d.register(Commands.literal("dictator").requires(OP)
                .then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player())
                        .executes(PoliticsCommands::dictatorAdd)))
                .then(Commands.literal("remove").executes(c -> { DictatorManager.removeDictator(c.getSource().getServer()); return 1; })));
        d.register(Commands.literal("summon").requires(PoliticsCommands::opOrJudge)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(PoliticsCommands::summon)));
    }

    // --- Handlers ---

    private static int vote(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer voter = c.getSource().getPlayerOrException();
        ServerPlayer candidate = EntityArgument.getPlayer(c, "player");
        if (ElectionManager.castVote(voter, candidate.getStringUUID())) {
            return ok(c, "Your vote for " + candidate.getName().getString() + " has been recorded.");
        }
        return fail(c, "Your vote could not be recorded (no active election, already voted, or invalid candidate).");
    }

    private static int gov(CommandContext<CommandSourceStack> c) {
        PoliticsData d = DataManager.data();
        Component msg = Component.literal("=== Government ===\n").withStyle(ChatFormatting.GOLD)
                .copy()
                .append(line("Chair", DataManager.nameOf(d.chair)))
                .append(line("Vice Chair", DataManager.nameOf(d.viceChair)))
                .append(line("Judge", DataManager.nameOf(d.judge)))
                .append(line("Dictator", d.dictatorActive ? DataManager.nameOf(d.dictator) : "none"))
                .append(line("Treasury", d.treasury + " coins"))
                .append(line("Election", d.electionActive ? "ACTIVE" : (d.electionSystemEnabled ? "scheduled" : "off")))
                .append(PerkManager.describeActive());
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static Component line(String key, String value) {
        return Component.literal(key + ": ").withStyle(ChatFormatting.GRAY)
                .copy().append(Component.literal(value + "\n").withStyle(ChatFormatting.WHITE));
    }

    private static int coins(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return ok(c, "Balance: " + DataManager.getCoins(p.getStringUUID()) + " coins.");
    }

    private static int pay(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer from = c.getSource().getPlayerOrException();
        ServerPlayer to = EntityArgument.getPlayer(c, "player");
        int amount = IntegerArgumentType.getInteger(c, "amount");
        if (!DataManager.removeCoins(from.getStringUUID(), amount)) return fail(c, "Insufficient coins.");
        DataManager.addCoins(to.getStringUUID(), amount);
        to.sendSystemMessage(Component.literal("You received " + amount + " coins from " + from.getName().getString() + ".")
                .withStyle(ChatFormatting.GREEN));
        return ok(c, "Sent " + amount + " coins to " + to.getName().getString() + ".");
    }

    private static int taxStatus(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        PoliticsData d = DataManager.data();
        return ok(c, "Tax: " + (d.taxEnabled ? d.taxPercent + "% daily" : "disabled")
                + ". You owe " + TaxManager.taxOwed(p.getStringUUID()) + " coins.");
    }

    private static int taxPay(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(c, "amount");
        return TaxManager.payTax(p, amount) ? ok(c, "Tax payment received.") : fail(c, "Nothing owed or insufficient coins.");
    }

    private static int setTax(CommandContext<CommandSourceStack> c, boolean enabled) {
        DataManager.data().taxEnabled = enabled;
        return ok(c, "Tax system " + (enabled ? "enabled." : "disabled."));
    }

    private static int taxCollect(CommandContext<CommandSourceStack> c) {
        TaxManager.collect(c.getSource().getServer());
        return 1;
    }

    private static int taxPercent(CommandContext<CommandSourceStack> c) {
        DataManager.data().taxPercent = IntegerArgumentType.getInteger(c, "percent");
        return ok(c, "Tax rate set to " + DataManager.data().taxPercent + "%.");
    }

    private static int setTaxTiers(CommandContext<CommandSourceStack> c, boolean enabled) {
        DataManager.data().taxTieredEnabled = enabled;
        return ok(c, "Progressive tax brackets " + (enabled
                ? "enabled (poverty exemption + wealth surcharge)." : "disabled (flat rate)."));
    }

    private static int treasury(CommandContext<CommandSourceStack> c) {
        return ok(c, "Treasury: " + DataManager.getTreasury() + " coins.");
    }

    private static int treasuryGive(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer to = EntityArgument.getPlayer(c, "player");
        int amount = IntegerArgumentType.getInteger(c, "amount");
        if (!DataManager.removeTreasury(amount)) return fail(c, "Treasury has insufficient funds.");
        DataManager.addCoins(to.getStringUUID(), amount);
        return ok(c, "Disbursed " + amount + " coins to " + to.getName().getString() + ".");
    }

    private static int impeachVote(CommandContext<CommandSourceStack> c, boolean yes) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!ElectionManager.castImpeachVote(p, yes)) return fail(c, "No active impeachment or already voted.");
        ElectionManager.resolveImpeachment(c.getSource().getServer());
        return ok(c, "Vote recorded.");
    }

    private static int setRole(CommandContext<CommandSourceStack> c, Role role) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        DataManager.registerPlayer(target);
        switch (role) {
            case CHAIR -> DataManager.setChair(target.getStringUUID());
            case VICE_CHAIR -> DataManager.setViceChair(target.getStringUUID());
            case JUDGE -> DataManager.setJudge(target.getStringUUID());
            default -> { }
        }
        return ok(c, target.getName().getString() + " is now " + role.title + ".");
    }

    private static int perkList(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("Perks (id: points):\n");
        for (Perk p : Perk.values()) {
            sb.append(p.name().toLowerCase()).append(" (").append(p.points >= 0 ? "+" : "").append(p.points)
                    .append(") - ").append(p.displayName).append('\n');
        }
        Component msg = Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY);
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int selectPerks(CommandContext<CommandSourceStack> c, boolean chair) {
        String raw = StringArgumentType.getString(c, "perks");
        List<Perk> perks = new ArrayList<>();
        for (String token : raw.split("\\s+")) {
            if (token.isBlank()) continue;
            Perk p = Perk.byId(token);
            if (p == null) return fail(c, "Unknown perk: " + token);
            perks.add(p);
        }
        boolean okSel = chair ? PerkManager.selectChairPerks(perks) : PerkManager.selectViceChairPerks(perks);
        if (!okSel) {
            return fail(c, chair
                    ? "Chair perks must sum to exactly 0 points and number at most " + PerkManager.CHAIR_MAX_PERKS + "."
                    : "Vice Chair must pick 1-2 perks not shared with the Chair.");
        }
        for (ServerPlayer pl : c.getSource().getServer().getPlayerList().getPlayers()) {
            PerkManager.applyActivePerks(pl);
        }
        return ok(c, "Perks applied.");
    }

    private static int imprisonHere(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int minutes = IntegerArgumentType.getInteger(c, "minutes");
        PrisonManager.imprison(target, minutes, target.getX(), target.getZ());
        return ok(c, "Imprisoned " + target.getName().getString() + " for " + minutes + " minute(s).");
    }

    private static int imprisonAt(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int minutes = IntegerArgumentType.getInteger(c, "minutes");
        int x = IntegerArgumentType.getInteger(c, "x");
        int z = IntegerArgumentType.getInteger(c, "z");
        PrisonManager.imprison(target, minutes, x, z);
        return ok(c, "Imprisoned " + target.getName().getString() + " for " + minutes + " minute(s).");
    }

    private static int pardon(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        PrisonManager.pardon(target.getStringUUID(), c.getSource().getServer());
        return ok(c, "Pardoned " + target.getName().getString() + ".");
    }

    private static int exile(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        PrisonManager.exile(target);
        return ok(c, "Exiled " + target.getName().getString() + ".");
    }

    private static int smite(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        if (!DictatorManager.canSmite()) return fail(c, "Smite is on cooldown.");
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        DictatorManager.smite(target);
        return ok(c, "Smote " + target.getName().getString() + ".");
    }

    private static int dictatorAdd(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        DictatorManager.setDictator(target, c.getSource().getServer());
        return 1;
    }

    private static int summon(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer caller = c.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        DictatorManager.summon(caller, target);
        return ok(c, "Summoned " + target.getName().getString() + ".");
    }

    // --- Helpers ---

    private static int ok(CommandContext<CommandSourceStack> c, String msg) {
        return ok(c, Component.literal(msg).withStyle(ChatFormatting.GREEN));
    }

    private static int ok(CommandContext<CommandSourceStack> c, Component msg) {
        c.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int fail(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendFailure(Component.literal(msg).withStyle(ChatFormatting.RED));
        return 0;
    }
}
