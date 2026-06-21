package com.political.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central custom sound registry for RPG Politics 2 (Expansion 3 sound design).
 *
 * <p>Every gameplay system in the mod currently reuses vanilla {@code SoundEvents}
 * ad&nbsp;hoc. This class defines a stable set of <em>named</em> mod-owned sound
 * events (e.g. {@code politicalserver:power_cast_fire}) that code can reference once
 * and forever, decoupling call sites from the specific audio chosen.</p>
 *
 * <p><b>Audio source.</b> We cannot ship real {@code .ogg} binaries here, so the
 * matching {@code assets/politicalserver/sounds.json} maps each custom id to one or
 * more layered <em>vanilla</em> sound events via {@code "type": "event"}. This means
 * every custom sound plays a sensible vanilla approximation today and nothing is ever
 * silent. To upgrade to bespoke audio later, drop real {@code .ogg} files into
 * {@code assets/politicalserver/sounds/} and flip the matching {@code sounds.json}
 * entries from {@code "type": "event"} to {@code "type": "file"} (see
 * {@code docs/expansion3/sounds.md}).</p>
 *
 * <p>Registration mirrors the project's existing registry pattern (see
 * {@code com.political.power.ModItems} / {@code com.political.curse.ModEntities}):
 * a {@link #register()} call wired from the common initializer, using
 * {@link Registry#register} against {@link BuiltInRegistries#SOUND_EVENT}.</p>
 */
public final class ModSounds {

    public static final String MOD_ID = "politicalserver";

    /** Lookup of short id (e.g. {@code "power_cast_fire"}) to its registered event. */
    private static final Map<String, SoundEvent> BY_ID = new LinkedHashMap<>();

    // ---- Power / ability casts ------------------------------------------------
    public static SoundEvent POWER_CAST_FIRE;
    public static SoundEvent POWER_CAST_FROST;
    public static SoundEvent POWER_CAST_VOID;
    public static SoundEvent POWER_CAST_LIGHTNING;
    public static SoundEvent POWER_CAST_HEAL;
    public static SoundEvent POWER_CAST_DOMAIN_OPEN;
    public static SoundEvent POWER_CAST_ULTIMATE;
    public static SoundEvent DOMAIN_COLLAPSE;
    public static SoundEvent HEAL_PULSE;
    public static SoundEvent SHIELD_BREAK;

    // ---- Melee / weapons ------------------------------------------------------
    public static SoundEvent MELEE_CRIT;
    public static SoundEvent WEAPON_SWING_HEAVY;
    public static SoundEvent MELEE_COMBO_FINISH;
    public static SoundEvent PARRY;

    // ---- Mobs / bosses --------------------------------------------------------
    public static SoundEvent SPIRIT_SCREECH;
    public static SoundEvent SPIRIT_ATTACK;
    public static SoundEvent SPIRIT_SUMMON;
    public static SoundEvent SPIRIT_DEATH;
    public static SoundEvent BOSS_ROAR;
    public static SoundEvent BOSS_PHASE;
    public static SoundEvent BOSS_SPAWN;

    // ---- GUI / UI -------------------------------------------------------------
    public static SoundEvent UI_OPEN;
    public static SoundEvent UI_CLICK;
    public static SoundEvent UI_CLOSE;
    public static SoundEvent UI_ERROR;
    public static SoundEvent UI_TAB;

    // ---- Dungeons -------------------------------------------------------------
    public static SoundEvent DUNGEON_AMBIENT;
    public static SoundEvent DUNGEON_TRAP;
    public static SoundEvent DUNGEON_CHEST_UNLOCK;
    public static SoundEvent DUNGEON_SECRET;
    public static SoundEvent DUNGEON_BOSS_GATE;

    // ---- Progression / economy ------------------------------------------------
    public static SoundEvent LEVEL_UP;
    public static SoundEvent RANK_UP;
    public static SoundEvent QUEST_COMPLETE;
    public static SoundEvent COIN_GAIN;
    public static SoundEvent COIN_LARGE;
    public static SoundEvent TRADE_COMPLETE;
    public static SoundEvent ELECTION_WIN;

    // ---- Movement / flight ----------------------------------------------------
    public static SoundEvent FLIGHT_BOOM;
    public static SoundEvent FLIGHT_LOOP;
    public static SoundEvent DASH;

    // ---- Curse system ---------------------------------------------------------
    public static SoundEvent CURSE_ABSORB;
    public static SoundEvent CURSE_APPLY;
    public static SoundEvent CURSE_CLEANSE;

    private ModSounds() {}

    /**
     * Registers every custom sound event. Call once from the common initializer
     * (alongside the other {@code *.register()} calls in {@code RpgPoliticsMod}).
     */
    public static void register() {
        POWER_CAST_FIRE = make("power_cast_fire");
        POWER_CAST_FROST = make("power_cast_frost");
        POWER_CAST_VOID = make("power_cast_void");
        POWER_CAST_LIGHTNING = make("power_cast_lightning");
        POWER_CAST_HEAL = make("power_cast_heal");
        POWER_CAST_DOMAIN_OPEN = make("power_cast_domain_open");
        POWER_CAST_ULTIMATE = make("power_cast_ultimate");
        DOMAIN_COLLAPSE = make("domain_collapse");
        HEAL_PULSE = make("heal_pulse");
        SHIELD_BREAK = make("shield_break");

        MELEE_CRIT = make("melee_crit");
        WEAPON_SWING_HEAVY = make("weapon_swing_heavy");
        MELEE_COMBO_FINISH = make("melee_combo_finish");
        PARRY = make("parry");

        SPIRIT_SCREECH = make("spirit_screech");
        SPIRIT_ATTACK = make("spirit_attack");
        SPIRIT_SUMMON = make("spirit_summon");
        SPIRIT_DEATH = make("spirit_death");
        BOSS_ROAR = make("boss_roar");
        BOSS_PHASE = make("boss_phase");
        BOSS_SPAWN = make("boss_spawn");

        UI_OPEN = make("ui_open");
        UI_CLICK = make("ui_click");
        UI_CLOSE = make("ui_close");
        UI_ERROR = make("ui_error");
        UI_TAB = make("ui_tab");

        DUNGEON_AMBIENT = make("dungeon_ambient");
        DUNGEON_TRAP = make("dungeon_trap");
        DUNGEON_CHEST_UNLOCK = make("dungeon_chest_unlock");
        DUNGEON_SECRET = make("dungeon_secret");
        DUNGEON_BOSS_GATE = make("dungeon_boss_gate");

        LEVEL_UP = make("level_up");
        RANK_UP = make("rank_up");
        QUEST_COMPLETE = make("quest_complete");
        COIN_GAIN = make("coin_gain");
        COIN_LARGE = make("coin_large");
        TRADE_COMPLETE = make("trade_complete");
        ELECTION_WIN = make("election_win");

        FLIGHT_BOOM = make("flight_boom");
        FLIGHT_LOOP = make("flight_loop");
        DASH = make("dash");

        CURSE_ABSORB = make("curse_absorb");
        CURSE_APPLY = make("curse_apply");
        CURSE_CLEANSE = make("curse_cleanse");
    }

    /** Creates + registers a single variable-range sound event and indexes it by id. */
    private static SoundEvent make(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        SoundEvent registered = Registry.register(BuiltInRegistries.SOUND_EVENT, id, event);
        BY_ID.put(name, registered);
        return registered;
    }

    /** Resolves a custom sound by its short id (e.g. {@code "ui_click"}); null if unknown. */
    public static SoundEvent byId(String name) {
        return BY_ID.get(name);
    }

    /** Immutable-ish view of every registered custom sound, keyed by short id. */
    public static Map<String, SoundEvent> all() {
        return BY_ID;
    }

    // =====================================================================
    //  play(...) helpers — the single entry point other systems should use.
    // =====================================================================

    /**
     * Plays a custom sound at a world position for everyone in range. Pass
     * {@code null} for {@code except} to also play it to the triggering player.
     */
    public static void play(Level level, double x, double y, double z, SoundEvent event,
                            SoundSource source, float volume, float pitch) {
        if (level == null || level.isClientSide() || event == null) return;
        level.playSound(null, x, y, z, event, source, volume, pitch);
    }

    /** Plays a custom sound at an entity's position (PLAYERS category, default 1.0/1.0). */
    public static void play(Level level, Entity at, SoundEvent event) {
        play(level, at, event, 1.0f, 1.0f);
    }

    /** Plays a custom sound at an entity's position with explicit volume/pitch. */
    public static void play(Level level, Entity at, SoundEvent event, float volume, float pitch) {
        if (at == null) return;
        SoundSource source = at instanceof ServerPlayer ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        play(level, at.getX(), at.getY(), at.getZ(), event, source, volume, pitch);
    }

    /** Convenience: play in the PLAYERS category at a position with default volume/pitch. */
    public static void play(Level level, double x, double y, double z, SoundEvent event) {
        play(level, x, y, z, event, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * Plays a personal UI/notification sound heard only by {@code player} (not the
     * world). Ideal for GUI open/click/close and progression chimes.
     */
    public static void playToPlayer(ServerPlayer player, SoundEvent event, float volume, float pitch) {
        if (player == null || event == null) return;
        player.playSound(event, volume, pitch);
    }

    /** Personal UI sound with default volume/pitch. */
    public static void playToPlayer(ServerPlayer player, SoundEvent event) {
        playToPlayer(player, event, 1.0f, 1.0f);
    }

    /**
     * Layers several custom/vanilla sounds at one position in a single call — a true
     * "stacked" sound (sounds.json variation entries only pick one at random, so this
     * is how callers get a genuinely layered effect when they want it).
     */
    public static void playLayered(Level level, double x, double y, double z,
                                   SoundSource source, float volume, float pitch, SoundEvent... events) {
        if (events == null) return;
        for (SoundEvent e : events) {
            play(level, x, y, z, e, source, volume, pitch);
        }
    }
}
