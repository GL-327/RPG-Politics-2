package com.political.civics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

import java.util.function.Predicate;

/** Command surface for the civics expansion: jobs, offices, laws, justice, factions, treasury fund. */
public final class CivicsCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

    private CivicsCommands() {}

    private static boolean opOrChair(CommandSourceStack s) {
        if (OP.test(s)) return true;
        ServerPlayer p = s.getPlayer();
        return p != null && DataManager.isChair(p.getUUID());
    }

    private static boolean opOrOffice(CommandSourceStack s, CivicOffice office) {
        if (OP.test(s)) return true;
        ServerPlayer p = s.getPlayer();
        return p != null && (DataManager.isChair(p.getUUID()) || OfficeManager.holds(p.getStringUUID(), office));
    }

    private static boolean opOrJudge(CommandSourceStack s) {
        if (opOrOffice(s, CivicOffice.JUDGE)) return true;
        ServerPlayer p = s.getPlayer();
        return p != null && DataManager.isJudge(p.getUUID());
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        registerJobs(d);
        registerOffices(d);
        registerLaws(d);
        registerJustice(d);
        registerFactions(d);
        registerFund(d);
        d.register(Commands.literal("civics").executes(CivicsCommands::overview));
    }

    // ---------------- Overview ----------------

    private static int overview(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String uuid = p.getStringUUID();
        StringBuilder sb = new StringBuilder("=== Civics ===\n");
        Job job = JobManager.jobOf(uuid);
        sb.append("Job: ").append(job == null ? "Unemployed" : job.displayName + " (Lv " + JobManager.level(uuid) + ")").append('\n');
        Faction f = FactionManager.factionOf(uuid);
        sb.append("Faction: ").append(f == null ? "None" : f.name).append('\n');
        sb.append("Laws in force: ").append(LawManager.summary()).append('\n');
        sb.append("Offices: ");
        for (CivicOffice o : CivicOffice.values()) {
            String h = OfficeManager.holder(o);
            sb.append(o.displayName).append('=').append(h.isEmpty() ? "Vacant" : DataManager.nameOf(h)).append("  ");
        }
        sb.append('\n');
        sb.append("Fund value: ").append(TreasuryFund.fundValue()).append(" coins\n");
        int fine = JusticeManager.outstandingFine(uuid);
        if (fine > 0) sb.append("Outstanding fine: ").append(fine).append(" coins (/fine pay)\n");
        if (JusticeManager.isWanted(uuid)) sb.append("You are WANTED: ").append(JusticeManager.wantedReward(uuid)).append(" coin bounty\n");
        return msg(c, sb.toString(), ChatFormatting.GOLD);
    }

    // ---------------- Jobs ----------------

    private static void registerJobs(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("job")
                .executes(CivicsCommands::jobStatus)
                .then(Commands.literal("status").executes(CivicsCommands::jobStatus))
                .then(Commands.literal("list").executes(CivicsCommands::jobList))
                .then(Commands.literal("work").executes(CivicsCommands::jobWork))
                .then(Commands.literal("quit").executes(CivicsCommands::jobQuit))
                .then(Commands.literal("join")
                        .then(Commands.argument("job", StringArgumentType.word())
                                .executes(CivicsCommands::jobJoin))));
        d.register(Commands.literal("jobs").executes(CivicsCommands::jobList));
    }

    private static int jobStatus(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String uuid = p.getStringUUID();
        Job job = JobManager.jobOf(uuid);
        if (job == null) return msg(c, "You are unemployed. Use /job list then /job join <job>.", ChatFormatting.GRAY);
        return msg(c, job.displayName + " - Level " + JobManager.level(uuid) + " ("
                + JobManager.xp(uuid) + "/" + JobManager.xpForNext(uuid) + " XP). Daily wage: "
                + JobManager.dailyWage(uuid) + " coins.", job.color);
    }

    private static int jobList(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("=== Jobs (/job join <id>) ===\n");
        for (Job j : Job.values()) {
            sb.append(j.id).append(" - ").append(j.displayName).append(" (").append(j.baseWage)
                    .append("/day): ").append(j.description).append('\n');
        }
        return msg(c, sb.toString(), ChatFormatting.GOLD);
    }

    private static int jobJoin(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        Job job = Job.byId(StringArgumentType.getString(c, "job"));
        if (job == null) return fail(c, "Unknown job. Use /job list.");
        JobManager.setJob(p, job);
        return msg(c, "You are now employed as a " + job.displayName + ".", ChatFormatting.GREEN);
    }

    private static int jobQuit(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return JobManager.quitJob(p.getStringUUID())
                ? msg(c, "You have left your job.", ChatFormatting.GRAY)
                : fail(c, "You don't have a job.");
    }

    private static int jobWork(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        long r = JobManager.work(p);
        if (r == -2) return fail(c, "Get a job first with /job join <id>.");
        if (r >= 0) return fail(c, "You're tired. Try again in " + r + "s.");
        return 1;
    }

    // ---------------- Offices ----------------

    private static void registerOffices(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("office")
                .executes(CivicsCommands::officeStatus)
                .then(Commands.literal("status").executes(CivicsCommands::officeStatus))
                .then(Commands.literal("start").requires(CivicsCommands::opOrChair)
                        .then(Commands.argument("office", StringArgumentType.word())
                                .executes(CivicsCommands::officeStart)))
                .then(Commands.literal("end").requires(CivicsCommands::opOrChair)
                        .executes(c -> { OfficeManager.endElection(c.getSource().getServer()); return msg(c, "Office election concluded.", ChatFormatting.GREEN); }))
                .then(Commands.literal("stand")
                        .then(Commands.argument("manifesto", StringArgumentType.greedyString())
                                .executes(CivicsCommands::officeStand)))
                .then(Commands.literal("vote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(CivicsCommands::officeVote)))
                .then(Commands.literal("fund")
                        .then(Commands.argument("coins", IntegerArgumentType.integer(1))
                                .executes(CivicsCommands::officeFund)))
                .then(Commands.literal("set").requires(OP)
                        .then(Commands.argument("office", StringArgumentType.word())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(CivicsCommands::officeSet)))));
    }

    private static int officeStatus(CommandContext<CommandSourceStack> c) {
        return msg(c, OfficeManager.standings(), ChatFormatting.GOLD);
    }

    private static int officeStart(CommandContext<CommandSourceStack> c) {
        CivicOffice office = CivicOffice.byId(StringArgumentType.getString(c, "office"));
        if (office == null) return fail(c, "Unknown office. Options: mayor, judge, treasurer.");
        return OfficeManager.startElection(c.getSource().getServer(), office)
                ? msg(c, "Campaign opened for " + office.displayName + ".", ChatFormatting.GREEN)
                : fail(c, "An office election is already running.");
    }

    private static int officeStand(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String manifesto = StringArgumentType.getString(c, "manifesto");
        return OfficeManager.stand(p, manifesto)
                ? msg(c, "You are standing for office. Campaign with /office fund <coins>.", ChatFormatting.GREEN)
                : fail(c, "No office election is currently open.");
    }

    private static int officeVote(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer voter = c.getSource().getPlayerOrException();
        ServerPlayer cand = EntityArgument.getPlayer(c, "player");
        return OfficeManager.vote(voter, cand.getStringUUID())
                ? msg(c, "Vote cast for " + cand.getName().getString() + ".", ChatFormatting.GREEN)
                : fail(c, "Vote rejected (no race, already voted, or not a candidate).");
    }

    private static int officeFund(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int coins = IntegerArgumentType.getInteger(c, "coins");
        return OfficeManager.pledge(p, coins)
                ? msg(c, "Pledged " + coins + " coins to your campaign.", ChatFormatting.GREEN)
                : fail(c, "Can't pledge (not a candidate, no race, or insufficient coins).");
    }

    private static int officeSet(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        CivicOffice office = CivicOffice.byId(StringArgumentType.getString(c, "office"));
        if (office == null) return fail(c, "Unknown office.");
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        DataManager.registerPlayer(target);
        OfficeManager.setHolder(office, target.getStringUUID());
        return msg(c, target.getName().getString() + " appointed " + office.displayName + ".", ChatFormatting.GREEN);
    }

    // ---------------- Laws ----------------

    private static void registerLaws(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("law")
                .executes(CivicsCommands::lawActive)
                .then(Commands.literal("list").executes(CivicsCommands::lawList))
                .then(Commands.literal("active").executes(CivicsCommands::lawActive))
                .then(Commands.literal("enact").requires(CivicsCommands::opOrChair)
                        .then(Commands.argument("law", StringArgumentType.word())
                                .executes(CivicsCommands::lawEnact)))
                .then(Commands.literal("repeal").requires(CivicsCommands::opOrChair)
                        .then(Commands.argument("law", StringArgumentType.word())
                                .executes(CivicsCommands::lawRepeal))));
    }

    private static int lawList(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("=== Laws (/law enact <id>) ===\n");
        for (CivicLaw l : CivicLaw.values()) {
            sb.append(l.id).append(" - ").append(l.displayName).append(" (").append(l.cost)
                    .append(" coins): ").append(l.description).append('\n');
        }
        return msg(c, sb.toString(), ChatFormatting.GOLD);
    }

    private static int lawActive(CommandContext<CommandSourceStack> c) {
        return msg(c, "Laws in force: " + LawManager.summary(), ChatFormatting.GOLD);
    }

    private static int lawEnact(CommandContext<CommandSourceStack> c) {
        CivicLaw law = CivicLaw.byId(StringArgumentType.getString(c, "law"));
        if (law == null) return fail(c, "Unknown law. Use /law list.");
        return LawManager.enact(c.getSource().getServer(), law)
                ? msg(c, law.displayName + " enacted.", ChatFormatting.GREEN)
                : fail(c, "Already in force, or treasury can't afford " + law.cost + " coins.");
    }

    private static int lawRepeal(CommandContext<CommandSourceStack> c) {
        CivicLaw law = CivicLaw.byId(StringArgumentType.getString(c, "law"));
        if (law == null) return fail(c, "Unknown law. Use /law list.");
        return LawManager.repeal(c.getSource().getServer(), law)
                ? msg(c, law.displayName + " repealed.", ChatFormatting.GRAY)
                : fail(c, "That law isn't in force.");
    }

    // ---------------- Justice ----------------

    private static void registerJustice(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("fine")
                .executes(CivicsCommands::fineStatus)
                .then(Commands.literal("status").executes(CivicsCommands::fineStatus))
                .then(Commands.literal("pay").executes(CivicsCommands::finePay))
                .then(Commands.literal("impose").requires(CivicsCommands::opOrJudge)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(CivicsCommands::fineImpose)))));

        d.register(Commands.literal("wanted")
                .executes(CivicsCommands::wantedList)
                .then(Commands.literal("list").executes(CivicsCommands::wantedList))
                .then(Commands.literal("post").requires(CivicsCommands::opOrJudge)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("reward", IntegerArgumentType.integer(1))
                                        .executes(CivicsCommands::wantedPost))))
                .then(Commands.literal("clear").requires(CivicsCommands::opOrJudge)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(CivicsCommands::wantedClear))));

        d.register(Commands.literal("bail")
                .executes(CivicsCommands::bailPay)
                .then(Commands.literal("pay").executes(CivicsCommands::bailPay))
                .then(Commands.literal("set").requires(CivicsCommands::opOrJudge)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(CivicsCommands::bailSet)))));
    }

    private static int fineStatus(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String uuid = p.getStringUUID();
        return msg(c, "Outstanding fine: " + JusticeManager.outstandingFine(uuid)
                + " coins. Criminal record: " + JusticeManager.record(uuid) + " offense(s).", ChatFormatting.GRAY);
    }

    private static int finePay(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int paid = JusticeManager.payFine(p);
        return paid > 0 ? msg(c, "Paid " + paid + " coins toward your fine.", ChatFormatting.GREEN)
                : fail(c, "Nothing owed, or you have no coins.");
    }

    private static int fineImpose(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int amount = IntegerArgumentType.getInteger(c, "amount");
        DataManager.registerPlayer(target);
        JusticeManager.fine(c.getSource().getServer(), target.getStringUUID(), amount);
        return msg(c, "Fined " + target.getName().getString() + " " + amount + " coins.", ChatFormatting.GREEN);
    }

    private static int wantedList(CommandContext<CommandSourceStack> c) {
        var wanted = DataManager.data().wanted;
        if (wanted.isEmpty()) return msg(c, "No active bounties.", ChatFormatting.GRAY);
        StringBuilder sb = new StringBuilder("=== Wanted ===\n");
        wanted.forEach((u, r) -> sb.append("- ").append(DataManager.nameOf(u)).append(": ").append(r).append(" coins\n"));
        return msg(c, sb.toString(), ChatFormatting.DARK_RED);
    }

    private static int wantedPost(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int reward = IntegerArgumentType.getInteger(c, "reward");
        DataManager.registerPlayer(target);
        JusticeManager.postWanted(c.getSource().getServer(), target.getStringUUID(), reward);
        return msg(c, "Bounty posted on " + target.getName().getString() + ".", ChatFormatting.GREEN);
    }

    private static int wantedClear(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int r = JusticeManager.clearWanted(target.getStringUUID());
        return r > 0 ? msg(c, "Cleared bounty on " + target.getName().getString() + ".", ChatFormatting.GREEN)
                : fail(c, "That player isn't wanted.");
    }

    private static int bailPay(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return JusticeManager.payBail(p)
                ? msg(c, "Bail paid — you are free.", ChatFormatting.GREEN)
                : fail(c, "You aren't jailed, no bail is set, or you can't afford it.");
    }

    private static int bailSet(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        int amount = IntegerArgumentType.getInteger(c, "amount");
        JusticeManager.setBail(target.getStringUUID(), amount);
        return msg(c, "Bail for " + target.getName().getString() + " set to " + amount + " coins.", ChatFormatting.GREEN);
    }

    // ---------------- Factions ----------------

    private static void registerFactions(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("faction")
                .executes(CivicsCommands::factionList)
                .then(Commands.literal("list").executes(CivicsCommands::factionList))
                .then(Commands.literal("info").executes(CivicsCommands::factionInfo))
                .then(Commands.literal("leave").executes(CivicsCommands::factionLeave))
                .then(Commands.literal("join")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(CivicsCommands::factionJoin)))
                .then(Commands.literal("donate")
                        .then(Commands.argument("coins", IntegerArgumentType.integer(1))
                                .executes(CivicsCommands::factionDonate)))
                .then(Commands.literal("motto")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(CivicsCommands::factionMotto)))
                .then(Commands.literal("found")
                        .then(Commands.argument("ideology", StringArgumentType.word())
                                .then(Commands.argument("tag", StringArgumentType.word())
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(CivicsCommands::factionFound))))));
    }

    private static int factionList(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder(FactionManager.list());
        sb.append("\nIdeologies: ");
        for (FactionIdeology i : FactionIdeology.values()) sb.append(i.name().toLowerCase()).append(' ');
        return msg(c, sb.toString(), ChatFormatting.GOLD);
    }

    private static int factionInfo(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        Faction f = FactionManager.factionOf(p.getStringUUID());
        if (f == null) return fail(c, "You aren't in a faction. See /faction list.");
        c.getSource().sendSuccess(() -> FactionManager.info(f), false);
        return 1;
    }

    private static int factionFound(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        FactionIdeology ideology = FactionIdeology.byId(StringArgumentType.getString(c, "ideology"));
        String tag = StringArgumentType.getString(c, "tag");
        String name = StringArgumentType.getString(c, "name");
        String result = FactionManager.found(p, name, tag, ideology);
        if ("INSUFFICIENT".equals(result)) return fail(c, "Founding a faction costs 2000 coins.");
        if (result == null) return fail(c, "You're already in a faction, or that name is taken.");
        return msg(c, "Founded " + name + " [" + ideology.displayName + "]! Recruit with /faction join " + result + ".", ChatFormatting.GREEN);
    }

    private static int factionJoin(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(c, "id");
        return FactionManager.join(p, id)
                ? msg(c, "You joined the faction.", ChatFormatting.GREEN)
                : fail(c, "Couldn't join (already in one, or no such faction).");
    }

    private static int factionLeave(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return FactionManager.leave(p)
                ? msg(c, "You left your faction.", ChatFormatting.GRAY)
                : fail(c, "You aren't in a faction.");
    }

    private static int factionDonate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int coins = IntegerArgumentType.getInteger(c, "coins");
        return FactionManager.donate(p, coins)
                ? msg(c, "Donated " + coins + " coins to the party war-chest.", ChatFormatting.GREEN)
                : fail(c, "Not in a faction, or insufficient coins.");
    }

    private static int factionMotto(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        Faction f = FactionManager.factionOf(p.getStringUUID());
        if (f == null) return fail(c, "You aren't in a faction.");
        if (!p.getStringUUID().equals(f.founder)) return fail(c, "Only the founder may set the motto.");
        f.motto = StringArgumentType.getString(c, "text");
        return msg(c, "Motto updated.", ChatFormatting.GREEN);
    }

    // ---------------- Treasury fund & public works ----------------

    private static void registerFund(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("fund")
                .executes(CivicsCommands::fundStatus)
                .then(Commands.literal("status").executes(CivicsCommands::fundStatus))
                .then(Commands.literal("invest").requires(s -> opOrOffice(s, CivicOffice.TREASURER))
                        .then(Commands.argument("coins", IntegerArgumentType.integer(1))
                                .executes(CivicsCommands::fundInvest)))
                .then(Commands.literal("divest").requires(s -> opOrOffice(s, CivicOffice.TREASURER))
                        .then(Commands.argument("coins", IntegerArgumentType.integer(1))
                                .executes(CivicsCommands::fundDivest))));

        d.register(Commands.literal("publicworks")
                .executes(CivicsCommands::worksStatus)
                .then(Commands.literal("status").executes(CivicsCommands::worksStatus))
                .then(Commands.literal("list").executes(CivicsCommands::worksStatus))
                .then(Commands.literal("fund").requires(s -> opOrOffice(s, CivicOffice.MAYOR))
                        .then(Commands.argument("project", StringArgumentType.word())
                                .then(Commands.argument("coins", IntegerArgumentType.integer(1))
                                        .executes(CivicsCommands::worksFund)))));
    }

    private static int fundStatus(CommandContext<CommandSourceStack> c) {
        return msg(c, TreasuryFund.summary(), ChatFormatting.GOLD);
    }

    private static int fundInvest(CommandContext<CommandSourceStack> c) {
        int coins = IntegerArgumentType.getInteger(c, "coins");
        return TreasuryFund.invest(coins)
                ? msg(c, "Invested " + coins + " coins into the sovereign wealth fund.", ChatFormatting.GREEN)
                : fail(c, "Treasury can't afford that.");
    }

    private static int fundDivest(CommandContext<CommandSourceStack> c) {
        int coins = IntegerArgumentType.getInteger(c, "coins");
        long realised = TreasuryFund.divest(coins);
        return realised > 0 ? msg(c, "Liquidated " + realised + " coins back to the treasury.", ChatFormatting.GREEN)
                : fail(c, "The fund holds nothing to liquidate.");
    }

    private static int worksStatus(CommandContext<CommandSourceStack> c) {
        return msg(c, TreasuryFund.summary(), ChatFormatting.GOLD);
    }

    private static int worksFund(CommandContext<CommandSourceStack> c) {
        PublicProject project = PublicProject.byId(StringArgumentType.getString(c, "project"));
        if (project == null) return fail(c, "Unknown project. See /publicworks status.");
        int coins = IntegerArgumentType.getInteger(c, "coins");
        int spent = TreasuryFund.fundProject(c.getSource().getServer(), project, coins);
        if (spent == -1) return fail(c, "That project is already complete.");
        return spent > 0 ? msg(c, "Channeled " + spent + " coins into " + project.displayName + ".", ChatFormatting.GREEN)
                : fail(c, "Treasury can't afford that, or nothing left to fund.");
    }

    // ---------------- Helpers ----------------

    private static int msg(CommandContext<CommandSourceStack> c, String text, ChatFormatting color) {
        Component m = Component.literal(text).withStyle(color);
        c.getSource().sendSuccess(() -> m, false);
        return 1;
    }

    private static int fail(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendFailure(Component.literal(text).withStyle(ChatFormatting.RED));
        return 0;
    }
}
