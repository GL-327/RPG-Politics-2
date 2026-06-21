package com.political.progression;

import com.political.curse.CurseEntity;
import com.political.curse.CurseManager;
import com.political.curse.CursedTrait;
import com.political.economy.BankManager;
import com.political.politics.CivicRank;
import com.political.politics.DataManager;
import com.political.politics.Role;
import com.political.power.Power;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * Server-side granter for the data-driven RPG progression tree.
 *
 * <p>The bulk of the advancement tree (see {@code data/politicalserver/advancement/**}) is driven by
 * <b>vanilla criteria</b> — {@code minecraft:inventory_changed} for collecting gear/serums and
 * {@code minecraft:player_killed_entity} for slaying dungeon and world bosses. Those need no Java.
 *
 * <p>A handful of goals depend on <b>mod-internal state that vanilla triggers cannot observe</b>
 * (a sorcerer's grade, which powers/techniques are known, citizenship, civic rank, bank balance,
 * the grade of an exorcised curse, …). Those advancements use the {@code minecraft:impossible}
 * trigger and are awarded here through {@link ServerPlayer#getAdvancements()}.
 *
 * <p><b>Integration:</b> call {@link #register()} once during mod init, alongside the other
 * {@code register()} calls in {@code RpgPoliticsMod#onInitialize}:
 * <pre>{@code com.political.progression.ProgressionManager.register();}</pre>
 * Nothing else is required — {@link #register()} installs its own Fabric event listeners:
 * <ul>
 *   <li>an {@code END_SERVER_TICK} poll (~2×/second) that reads each online player's persistent
 *       state and grants any state-based advancement whose condition is now met (idempotent);</li>
 *   <li>an {@code AFTER_DEATH} listener that detects curse exorcisms and grants the Hunter
 *       grade advancements based on the slain curse's grade.</li>
 * </ul>
 *
 * <p>Optional explicit hooks ({@link #onVote}, {@link #onTaxPaid}, {@link #onCurseExorcised},
 * {@link #onDungeonBossDefeated}, {@link #refresh}) are provided for callers that want to award a
 * node the instant an action happens rather than waiting for the next poll. They are safe to call
 * from anywhere on the server thread and are idempotent.
 */
public final class ProgressionManager {

    public static final String MOD_ID = "politicalserver";

    /** Criterion name used by every {@code minecraft:impossible} advancement in the tree. */
    private static final String GRANTED = "granted";

    /** Poll cadence in ticks (40 ticks = ~2 seconds). */
    private static final int POLL_INTERVAL = 40;

    private static boolean registered = false;

    private ProgressionManager() {}

    public static void register() {
        if (registered) return;
        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % POLL_INTERVAL != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                try {
                    poll(player);
                } catch (Exception ignored) {
                    // Never let progression bookkeeping break the server tick.
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(source.getEntity() instanceof ServerPlayer killer)) return;
            if (CurseManager.isCurse(entity)) {
                onCurseExorcised(killer, gradeOf(entity));
            }
        });
    }

    // ------------------------------------------------------------------
    // State poll — grants every state-based advancement currently earned.
    // ------------------------------------------------------------------

    /** Re-evaluates and grants all state-based advancements for a single player. Cheap + idempotent. */
    public static void refresh(ServerPlayer player) {
        try {
            poll(player);
        } catch (Exception ignored) {
        }
    }

    private static void poll(ServerPlayer player) {
        String uuid = player.getStringUUID();

        // ---- Sorcery: grades, awakening, techniques, six eyes, domain ----
        int grade = DataManager.sorcererGrade(uuid);
        if (grade >= 1) { grant(player, "sorcery/root"); grant(player, "sorcery/grade_4"); }
        if (grade >= 2) grant(player, "sorcery/grade_3");
        if (grade >= 3) grant(player, "sorcery/grade_2");
        if (grade >= 4) grant(player, "sorcery/grade_1");
        if (grade >= 5) grant(player, "sorcery/special_grade");

        List<String> powers = DataManager.knownPowers(uuid);
        boolean knowsTechnique = false;
        boolean knowsCompoundV = false;
        for (String id : powers) {
            Power p = Power.byId(id);
            if (p == null) continue;
            if (p.origin == Power.Origin.CURSED_TECHNIQUE) knowsTechnique = true;
            if (p.origin == Power.Origin.COMPOUND_V) knowsCompoundV = true;
        }
        if (knowsTechnique) grant(player, "sorcery/learn_technique");
        if (has(uuid, "black_flash")) grant(player, "sorcery/black_flash");
        if (DataManager.cursedTrait(uuid) == CursedTrait.SIX_EYES || has(uuid, "six_eyes")) {
            grant(player, "sorcery/six_eyes");
        }
        if (has(uuid, "domain_expansion") || has(uuid, "malevolent_shrine")
                || has(uuid, "self_embodiment") || has(uuid, "chimera_shadow_garden")
                || has(uuid, "coffin_iron_mountain")) {
            grant(player, "sorcery/domain_expansion");
        }

        // ---- Hero: Compound V manifestation, flight, viltrumite, archetypes ----
        if (knowsCompoundV) grant(player, "hero/manifest");
        if (has(uuid, "super_strength")) grant(player, "hero/super_strength");
        if (has(uuid, "laser_eyes") || has(uuid, "heat_vision_overload")) grant(player, "hero/laser_eyes");
        if (has(uuid, "speedster") || has(uuid, "afterimage")) grant(player, "hero/speedster");
        if (has(uuid, "healing") || has(uuid, "regen_surge") || has(uuid, "regenerative_code")) {
            grant(player, "hero/healing_factor");
        }
        if (has(uuid, "flight") || has(uuid, "star_power") || has(uuid, "icarus_dive")) {
            grant(player, "hero/flight");
        }
        if (has(uuid, "icarus_dive") || has(uuid, "ground_pound")
                || has(uuid, "titan_grip") || has(uuid, "meteor_drop")) {
            grant(player, "hero/viltrumite");
        }
        if (has(uuid, "star_power")) grant(player, "hero/star_power");

        // ---- Politics & economy ----
        if (DataManager.citizenshipOf(uuid) != null) grant(player, "politics/root");
        if (DataManager.data().votedPlayers.containsKey(uuid)) grant(player, "politics/vote");
        int rank = DataManager.civicRank(uuid).ordinal();
        if (rank >= CivicRank.OFFICER.ordinal()) grant(player, "politics/officer");
        if (rank >= CivicRank.COUNCILOR.ordinal()) grant(player, "politics/councilor");
        if (DataManager.roleOf(uuid) != Role.NONE) grant(player, "politics/office");
        if (DataManager.data().taxEnabled && DataManager.citizenshipOf(uuid) != null) {
            grant(player, "politics/civic_duty");
        }
        int bank = BankManager.balance(uuid);
        if (bank >= 1_000) grant(player, "politics/bank_1k");
        if (bank >= 10_000) grant(player, "politics/bank_10k");
        if (bank >= 100_000) grant(player, "politics/bank_100k");
        if (DataManager.netWorth(uuid) >= 1_000_000L) grant(player, "politics/tycoon");

        // ---- Hunter: lifetime exorcism milestones (grade-specific kills handled on death) ----
        int exorcised = DataManager.data().cursesExorcised.getOrDefault(uuid, 0);
        if (exorcised >= 1) grant(player, "hunter/root");
        if (exorcised >= 50) grant(player, "hunter/veteran");
    }

    // ------------------------------------------------------------------
    // Explicit integration hooks (optional, idempotent)
    // ------------------------------------------------------------------

    /** Award the "cast a ballot" advancement. Call from the voting code if you want instant feedback. */
    public static void onVote(ServerPlayer player) {
        grant(player, "politics/vote");
    }

    /** Award the "civic duty / taxpayer" advancement when a player contributes to the treasury. */
    public static void onTaxPaid(ServerPlayer player) {
        grant(player, "politics/civic_duty");
    }

    /**
     * Award the Hunter advancements for exorcising a curse of the given <b>internal</b> grade (1..5,
     * where 5 is Special Grade). Called automatically from the {@code AFTER_DEATH} listener; also
     * exposed so {@code CurseManager} can call it directly if preferred.
     */
    public static void onCurseExorcised(ServerPlayer player, int internalGrade) {
        grant(player, "hunter/root");
        if (internalGrade >= 3) grant(player, "hunter/grade_2_curse");
        if (internalGrade >= 4) grant(player, "hunter/grade_1_curse");
        if (internalGrade >= 5) grant(player, "hunter/special_grade_curse");
    }

    /**
     * Award the Explorer advancement for a dungeon boss kill, keyed by {@code DungeonType.id}
     * (e.g. {@code "cursed_crypt"}). Optional: the dungeon-boss advancements are already wired to
     * fire on the vanilla {@code player_killed_entity} trigger, so this is only needed if a boss is
     * killed in a way that does not register as a player kill.
     */
    public static void onDungeonBossDefeated(ServerPlayer player, String dungeonTypeId) {
        if (dungeonTypeId == null) return;
        grant(player, "explorer/" + dungeonTypeId);
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private static boolean has(String uuid, String powerId) {
        return DataManager.hasPower(uuid, powerId);
    }

    private static int gradeOf(LivingEntity entity) {
        return entity instanceof CurseEntity c ? c.getGrade() : 1;
    }

    /**
     * Completes the advancement {@code politicalserver:<path>} for the player by awarding all of its
     * remaining criteria. No-op if the advancement is unknown or already done.
     */
    private static void grant(ServerPlayer player, String path) {
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        AdvancementHolder holder = server.getAdvancements()
                .get(Identifier.fromNamespaceAndPath(MOD_ID, path));
        if (holder == null) return;
        PlayerAdvancements advancements = player.getAdvancements();
        AdvancementProgress progress = advancements.getOrStartProgress(holder);
        if (progress.isDone()) return;
        // Award the conventional single criterion first, then any stragglers.
        advancements.award(holder, GRANTED);
        for (String criterion : progress.getRemainingCriteria()) {
            advancements.award(holder, criterion);
        }
    }
}
