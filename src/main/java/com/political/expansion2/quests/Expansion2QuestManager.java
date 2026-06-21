package com.political.expansion2.quests;

import com.political.bounty.BountyManager;
import com.political.bounty.BountyType;
import com.political.combat.StatManager;
import com.political.curse.spirits.CursedSpiritEntity;
import com.political.curse.spirits.SpiritSpecies;
import com.political.expansion2.curses.CursedSpirit2Entity;
import com.political.economy.BankManager;
import com.political.expansion2.npc.NamedNpcBoss;
import com.political.expansion2.npc.NpcArchetype;
import com.political.politics.DataManager;
import com.political.politics.ElectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/** Tracks and completes 55+ Expansion 2 quests via public APIs. */
public final class Expansion2QuestManager {

    private static final Random RNG = new Random();

    private Expansion2QuestManager() {}

    public static String accept(ServerPlayer player, String questId) {
        QuestDef q = QuestRegistry.get(questId);
        if (q == null) return "Unknown quest: " + questId;
        String uuid = player.getStringUUID();
        Expansion2QuestData d = Expansion2QuestStorage.data();
        if (d.active.containsKey(uuid)) return "Finish your current contract first (/exp2quest abandon).";
        if (!q.repeatable() && d.completedList(uuid).contains(questId)) {
            return "You already completed " + q.title() + ".";
        }
        if (q.kind() == QuestKind.BOUNTY) {
            BountyType bt = BountyType.byId(q.target());
            if (bt == null) return "Invalid bounty type.";
            if (!BountyManager.startQuest(player, bt, 1)) return "You already have a slayer quest active.";
        }
        d.active.put(uuid, q.id() + "|0|" + System.currentTimeMillis());
        return "Accepted: " + q.title() + " — " + q.description();
    }

    public static String tryTurnIn(ServerPlayer player, NpcArchetype arch) {
        String uuid = player.getStringUUID();
        Expansion2QuestData d = Expansion2QuestStorage.data();
        String raw = d.active.get(uuid);
        if (raw == null) return "No active contract.";
        String[] p = raw.split("\\|");
        QuestDef q = QuestRegistry.get(p[0]);
        if (q == null) {
            d.active.remove(uuid);
            return "Stale quest cleared.";
        }
        if (q.giver() != arch) {
            return "This NPC doesn't handle that contract. Seek " + q.giver().title + ".";
        }
        if (!isComplete(player, q, parseProgress(p))) {
            return progressMessage(player, q, parseProgress(p));
        }
        complete(player, q);
        return "Turned in: " + q.title() + "! Rewards granted.";
    }

    public static String listOffersFor(ServerPlayer player, NpcArchetype arch) {
        List<QuestDef> qs = QuestRegistry.forGiver(arch);
        if (qs.isEmpty()) return "No contracts from this contact.";
        StringBuilder sb = new StringBuilder("Contracts (use /exp2quest accept <id>): ");
        int n = 0;
        for (QuestDef q : qs) {
            if (n++ >= 6) { sb.append("…"); break; }
            sb.append(q.id()).append(", ");
        }
        return sb.toString();
    }

    public static void offerRandomForArchetype(ServerPlayer player, NpcArchetype arch) {
        List<QuestDef> qs = QuestRegistry.forGiver(arch);
        if (qs.isEmpty()) return;
        QuestDef q = qs.get(RNG.nextInt(qs.size()));
        accept(player, q.id());
    }

    public static void onEntityDeath(LivingEntity victim, ServerPlayer killer) {
        String uuid = killer.getStringUUID();
        Expansion2QuestData d = Expansion2QuestStorage.data();
        String raw = d.active.get(uuid);
        if (raw == null) return;
        QuestDef q = QuestRegistry.get(raw.split("\\|")[0]);
        if (q == null) return;
        int prog = parseProgress(raw.split("\\|"));
        if (q.kind() == QuestKind.KILL_SPIRIT) {
            String spiritId = spiritIdOf(victim);
            if (spiritId != null && spiritMatchesQuest(spiritId, q.target(), victim)) {
                bump(killer, q, prog + 1);
            }
        } else if (q.kind() == QuestKind.KILL_MOB) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
            if (id != null && id.toString().equals(q.target())) {
                bump(killer, q, prog + 1);
            }
        }
    }

    public static void onBossDefeated(ServerPlayer killer, NamedNpcBoss boss) {
        String uuid = killer.getStringUUID();
        Expansion2QuestData d = Expansion2QuestStorage.data();
        d.bossesDefeated.computeIfAbsent(uuid, k -> new java.util.HashMap<>())
                .merge(boss.id, 1, Integer::sum);
        String raw = d.active.get(uuid);
        if (raw == null) return;
        QuestDef q = QuestRegistry.get(raw.split("\\|")[0]);
        if (q != null && q.kind() == QuestKind.BOSS && q.target().equals(boss.id)) {
            bump(killer, q, 1);
        }
    }

    public static void tick(ServerPlayer player) {
        String uuid = player.getStringUUID();
        Expansion2QuestData d = Expansion2QuestStorage.data();
        String raw = d.active.get(uuid);
        if (raw == null) return;
        String[] p = raw.split("\\|");
        QuestDef q = QuestRegistry.get(p[0]);
        if (q == null) return;
        int prog = parseProgress(p);
        boolean auto = switch (q.kind()) {
            case BANK_DEPOSIT -> BankManager.balance(uuid) >= q.amount();
            case COLLECT_COINS -> {
                if (q.target().startsWith("pay:")) {
                    int need = Integer.parseInt(q.target().substring(4));
                    yield DataManager.getCoins(uuid) >= need;
                }
                yield DataManager.getCoins(uuid) >= q.amount();
            }
            case AWAKEN_CE -> q.target().equals("max")
                    ? StatManager.getCursedEnergy(player) >= StatManager.getMaxCursedEnergy(player)
                    : StatManager.getCursedEnergy(player) >= q.amount();
            case EXORCISE_COUNT -> DataManager.data().cursesExorcised.getOrDefault(uuid, 0) >= q.amount();
            case REACH_GRADE -> DataManager.sorcererGrade(uuid) >= q.amount();
            case ELECTION -> ElectionManager.isElectionActive()
                    && Boolean.TRUE.equals(d.votedDuringQuest.get(uuid));
            case FETCH, DELIVER, HOLD_ITEM -> hasItems(player, q);
            default -> false;
        };
        if (auto && prog < q.amount()) {
            int newProg = switch (q.kind()) {
                case FETCH, DELIVER, HOLD_ITEM -> q.amount();
                case COLLECT_COINS -> q.amount();
                default -> q.amount();
            };
            d.active.put(uuid, q.id() + "|" + newProg + "|" + p[2]);
            player.sendSystemMessage(Component.literal("Quest ready to turn in: " + q.title())
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    public static void onVote(ServerPlayer voter) {
        Expansion2QuestStorage.data().votedDuringQuest.put(voter.getStringUUID(), true);
    }

    public static boolean abandon(ServerPlayer player) {
        return Expansion2QuestStorage.data().active.remove(player.getStringUUID()) != null;
    }

    public static String status(ServerPlayer player) {
        String uuid = player.getStringUUID();
        String raw = Expansion2QuestStorage.data().active.get(uuid);
        if (raw == null) return "No active Expansion 2 quest.";
        String[] p = raw.split("\\|");
        QuestDef q = QuestRegistry.get(p[0]);
        if (q == null) return "Unknown active quest.";
        return q.title() + ": " + progressMessage(player, q, parseProgress(p));
    }

    private static void bump(ServerPlayer player, QuestDef q, int newProg) {
        String uuid = player.getStringUUID();
        Expansion2QuestData d = Expansion2QuestStorage.data();
        String raw = d.active.get(uuid);
        String ts = raw != null && raw.split("\\|").length > 2 ? raw.split("\\|")[2] : "0";
        d.active.put(uuid, q.id() + "|" + newProg + "|" + ts);
        if (newProg >= q.amount()) {
            player.sendSystemMessage(Component.literal("Objective complete for: " + q.title()
                    + ". Return to " + q.giver().title + ".").withStyle(ChatFormatting.GOLD));
        } else if (newProg % 4 == 0) {
            player.sendSystemMessage(Component.literal(q.title() + " progress: " + newProg + "/" + q.amount())
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void complete(ServerPlayer player, QuestDef q) {
        String uuid = player.getStringUUID();
        Expansion2QuestData d = Expansion2QuestStorage.data();
        if (q.kind() == QuestKind.FETCH || q.kind() == QuestKind.DELIVER) {
            consumeItems(player, q);
        }
        if (q.coinReward() > 0) DataManager.addCoins(uuid, q.coinReward());
        if (q.creditReward() > 0) DataManager.addCredits(uuid, q.creditReward());
        d.active.remove(uuid);
        if (!q.repeatable()) d.completedList(uuid).add(q.id());
        player.sendSystemMessage(Component.literal("Quest complete: " + q.title()
                + " (+" + q.coinReward() + " coin)").withStyle(ChatFormatting.GREEN));
    }

    private static boolean isComplete(ServerPlayer player, QuestDef q, int prog) {
        return switch (q.kind()) {
            case FETCH, DELIVER -> hasItems(player, q);
            case KILL_SPIRIT, KILL_MOB, BOSS -> prog >= q.amount();
            case BANK_DEPOSIT -> BankManager.balance(player.getStringUUID()) >= q.amount();
            case COLLECT_COINS -> {
                if (q.target().startsWith("pay:")) {
                    yield DataManager.getCoins(player.getStringUUID()) >= Integer.parseInt(q.target().substring(4));
                }
                yield DataManager.getCoins(player.getStringUUID()) >= q.amount();
            }
            case AWAKEN_CE -> q.target().equals("max")
                    ? StatManager.getCursedEnergy(player) >= StatManager.getMaxCursedEnergy(player)
                    : StatManager.getCursedEnergy(player) >= q.amount();
            case EXORCISE_COUNT -> DataManager.data().cursesExorcised.getOrDefault(player.getStringUUID(), 0) >= q.amount();
            case REACH_GRADE -> DataManager.sorcererGrade(player.getStringUUID()) >= q.amount();
            case ELECTION -> Boolean.TRUE.equals(Expansion2QuestStorage.data().votedDuringQuest.get(player.getStringUUID()));
            case BOUNTY -> BountyManager.questStatus(player.getStringUUID()).contains("boss summoned")
                    || !DataManager.data().activeQuests.containsKey(player.getStringUUID());
            case DEPOSIT_TREASURY -> prog >= q.amount();
            case HOLD_ITEM -> hasItems(player, q);
        };
    }

    private static boolean hasItems(ServerPlayer player, QuestDef q) {
        if (q.target().startsWith("minecraft:")) {
            Identifier id = Identifier.parse(q.target());
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item == null) return false;
            return countItem(player, item) >= q.amount();
        }
        Item questItem = QuestItems.get(q.target());
        if (questItem == null) return false;
        return countItem(player, questItem) >= q.amount();
    }

    private static void consumeItems(ServerPlayer player, QuestDef q) {
        Item item;
        if (q.target().startsWith("minecraft:")) {
            item = BuiltInRegistries.ITEM.getValue(Identifier.parse(q.target()));
        } else {
            item = QuestItems.get(q.target());
        }
        if (item == null) return;
        int need = q.amount();
        for (int i = 0; i < player.getInventory().getContainerSize() && need > 0; i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (st.is(item)) {
                int take = Math.min(need, st.getCount());
                st.shrink(take);
                need -= take;
            }
        }
    }

    private static int countItem(ServerPlayer player, Item item) {
        int n = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (st.is(item)) n += st.getCount();
        }
        return n;
    }

    private static int parseProgress(String[] parts) {
        return parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    }

    private static String spiritIdOf(LivingEntity victim) {
        if (victim instanceof CursedSpiritEntity spirit && spirit.species() != null) {
            return spirit.species().id();
        }
        if (victim instanceof CursedSpirit2Entity spirit2 && spirit2.species() != null) {
            return spirit2.species().id();
        }
        return null;
    }

    /**
     * Phase-1 quest targets (e.g. {@code curse_wisp}) also accept matching Phase-2 spirit species
     * so exorcism contracts progress against the expanded roster.
     */
    private static boolean spiritMatchesQuest(String spiritId, String target, LivingEntity victim) {
        if (spiritId.equals(target)) return true;
        if (!spiritId.startsWith("spirit2_")) return false;
        int grade = victim instanceof CursedSpirit2Entity s2 ? s2.getGrade() : 1;
        return switch (target) {
            case "curse_wisp" -> grade <= 1 && (spiritId.contains("wisp") || spiritId.contains("mote")
                    || spiritId.contains("gnat") || spiritId.contains("flea"));
            case "grudge_larva" -> grade <= 1 && spiritId.contains("larva");
            case "shadow_imp" -> grade <= 2 && spiritId.contains("imp");
            case "slitmouth_curse" -> grade <= 2 && (spiritId.contains("mouth") || spiritId.contains("grin"));
            case "brute_curse" -> grade <= 3 && (spiritId.contains("brute") || spiritId.contains("ogre")
                    || spiritId.contains("hulk"));
            case "spitter_curse" -> grade <= 3 && (spiritId.contains("spit") || spiritId.contains("venom")
                    || spiritId.contains("acid"));
            case "horned_curse" -> grade <= 3 && spiritId.contains("horn");
            case "veil_curse" -> grade <= 3 && (spiritId.contains("veil") || spiritId.contains("shade")
                    || spiritId.contains("wraith"));
            case "plague_curse" -> grade <= 4 && (spiritId.contains("plague") || spiritId.contains("rot")
                    || spiritId.contains("mold"));
            case "ember_curse" -> grade <= 4 && (spiritId.contains("ember") || spiritId.contains("ash")
                    || spiritId.contains("cinder"));
            case "flame_calamity" -> grade >= 4 && (spiritId.contains("calamity") || spiritId.contains("inferno")
                    || spiritId.contains("blaze"));
            case "transfigured_soul" -> grade >= 4 && (spiritId.contains("soul") || spiritId.contains("transfig")
                    || spiritId.contains("bound"));
            default -> false;
        };
    }

    private static String progressMessage(ServerPlayer player, QuestDef q, int prog) {
        String uuid = player.getStringUUID();
        return switch (q.kind()) {
            case KILL_SPIRIT, KILL_MOB, BOSS -> prog + "/" + q.amount() + " slain";
            case FETCH, DELIVER, HOLD_ITEM -> countLabel(q);
            case BANK_DEPOSIT -> "Bank balance need: " + q.amount() + " (have " + BankManager.balance(uuid) + ")";
            case COLLECT_COINS -> "Coin requirement: " + q.amount();
            case AWAKEN_CE -> "CE: " + (int) StatManager.getCursedEnergy(player) + "/" + q.amount();
            case EXORCISE_COUNT -> "Exorcised: "
                    + DataManager.data().cursesExorcised.getOrDefault(uuid, 0) + "/" + q.amount();
            case REACH_GRADE -> "Grade " + DataManager.sorcererGrade(uuid) + "/" + q.amount();
            case ELECTION -> "Vote during an election";
            case BOUNTY -> BountyManager.questStatus(uuid);
            case DEPOSIT_TREASURY -> "Donate " + q.amount() + " to treasury";
        };
    }

    private static String countLabel(QuestDef q) {
        return "Bring " + q.amount() + "x " + q.target();
    }
}
