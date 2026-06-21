package com.political.expansion2.curses;

import net.minecraft.ChatFormatting;

import java.util.EnumSet;
import java.util.Set;

/**
 * Phase-2 cursed spirit roster ({@value #COUNT} species). Entity ids are prefixed {@code spirit2_}.
 * Generated from tools/spirit2-species-data.js — edit the data file and re-run {@code gen-spirit2-enum.js}.
 */
public enum SpiritSpecies2 {

    MOTE_FLEA("spirit2_mote_flea", "Cursed Mote Flea", 1, 12, 2, 0.38, 0.45f, 0,
            ModelKind.SWARM_TINY, 0.45f, 0.7f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FAST_SWARM)),

    GRUDGE_GNAT("spirit2_grudge_gnat", "Grudge Gnat", 1, 13, 2, 0.36, 0.48f, 0,
            ModelKind.SWARM_TINY, 0.48f, 0.75f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FAST_SWARM, Behavior2.POISON_AURA)),

    INK_LARVA("spirit2_ink_larva", "Ink Larva", 1, 14, 3, 0.32, 0.5f, 0,
            ModelKind.SWARM_TINY, 0.5f, 0.8f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.BLINDNESS_CURSE)),

    SOOT_WISP("spirit2_soot_wisp", "Soot Wisp", 1, 11, 2, 0.4, 0.42f, 0,
            ModelKind.SWARM_TINY, 0.42f, 0.65f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FAST_SWARM, Behavior2.TELEPORT)),

    BONE_FLEA("spirit2_bone_flea", "Bone Flea", 1, 15, 3, 0.33, 0.5f, 0.05,
            ModelKind.SWARM_TINY, 0.5f, 0.8f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.WITHER_TOUCH)),

    ASH_CRAWLER("spirit2_ash_crawler", "Ash Crawler", 1, 16, 3, 0.28, 0.52f, 0.05,
            ModelKind.SWARM_TINY, 0.52f, 0.85f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FIRE_BLAST)),

    MOLD_SPRITE("spirit2_mold_sprite", "Mold Sprite", 1, 14, 2, 0.34, 0.48f, 0,
            ModelKind.SWARM_TINY, 0.48f, 0.75f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.POISON_AURA)),

    RUST_GNAT("spirit2_rust_gnat", "Rust Gnat", 1, 13, 3, 0.35, 0.46f, 0,
            ModelKind.SWARM_TINY, 0.46f, 0.72f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.CURSE_SEAL)),

    TAR_IMP("spirit2_tar_imp", "Tar Imp", 1, 17, 4, 0.26, 0.55f, 0.1,
            ModelKind.GAUNT, 0.55f, 1.1f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FROST_AURA)),

    DUST_WRAITHLING("spirit2_dust_wraithling", "Dust Wraithling", 1, 12, 2, 0.37, 0.44f, 0,
            ModelKind.SWARM_TINY, 0.44f, 0.68f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.TELEPORT, Behavior2.FAST_SWARM)),

    SCAB_SWARM("spirit2_scab_swarm", "Scab Swarm", 1, 15, 3, 0.3, 0.5f, 0,
            ModelKind.SWARM_TINY, 0.5f, 0.8f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.POISON_AURA, Behavior2.LIFE_DRAIN)),

    FILTH_HORNET("spirit2_filth_hornet", "Filth Hornet", 1, 14, 3, 0.36, 0.47f, 0,
            ModelKind.SWARM_TINY, 0.47f, 0.74f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FAST_SWARM, Behavior2.RANGED_BLAST)),

    ROT_TICK("spirit2_rot_tick", "Rot Tick", 1, 16, 3, 0.27, 0.52f, 0.05,
            ModelKind.SWARM_TINY, 0.52f, 0.82f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.WITHER_TOUCH, Behavior2.POISON_AURA)),

    GLASS_SHARDLING("spirit2_glass_shardling", "Glass Shardling", 1, 13, 4, 0.34, 0.46f, 0,
            ModelKind.SWARM_TINY, 0.46f, 0.72f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.RANGED_BLAST)),

    NAIL_CRAWLER("spirit2_nail_crawler", "Nail Crawler", 1, 15, 4, 0.29, 0.5f, 0.08,
            ModelKind.SWARM_TINY, 0.5f, 0.8f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.MELEE_BRUISER)),

    THREAD_LARVA("spirit2_thread_larva", "Thread Larva", 1, 14, 2, 0.32, 0.48f, 0,
            ModelKind.SWARM_TINY, 0.48f, 0.76f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FROST_AURA, Behavior2.CURSE_SEAL)),

    ECHO_MOTE("spirit2_echo_mote", "Echo Mote", 1, 12, 2, 0.39, 0.43f, 0,
            ModelKind.SWARM_TINY, 0.43f, 0.66f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.VOICE_CURSE)),

    SMOKE_CRAWLER("spirit2_smoke_crawler", "Smoke Crawler", 1, 15, 3, 0.31, 0.5f, 0,
            ModelKind.SWARM_TINY, 0.5f, 0.8f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.BLINDNESS_CURSE, Behavior2.TELEPORT)),

    PESTILENT_GNAT("spirit2_pestilent_gnat", "Pestilent Gnat", 1, 14, 3, 0.35, 0.47f, 0,
            ModelKind.SWARM_TINY, 0.47f, 0.74f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.POISON_AURA, Behavior2.SWARM_REPLICATE)),

    MAGGOT_HUSK("spirit2_maggot_husk", "Maggot Husk", 1, 17, 3, 0.24, 0.54f, 0.1,
            ModelKind.CORPSE, 0.54f, 0.9f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.NECROMANCY_RISE)),

    CINDER_FLEA("spirit2_cinder_flea", "Cinder Flea", 1, 13, 3, 0.36, 0.46f, 0,
            ModelKind.SWARM_TINY, 0.46f, 0.72f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FIRE_BLAST, Behavior2.FIRE_IMMUNE)),

    FROST_MOTE("spirit2_frost_mote", "Frost Mote", 1, 14, 2, 0.33, 0.48f, 0,
            ModelKind.SWARM_TINY, 0.48f, 0.76f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FROST_AURA)),

    SALT_SPRITE("spirit2_salt_sprite", "Salt Sprite", 1, 15, 3, 0.3, 0.5f, 0.05,
            ModelKind.SWARM_TINY, 0.5f, 0.8f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.CURSE_SEAL)),

    CLAW_LARVA("spirit2_claw_larva", "Claw Larva", 1, 16, 4, 0.28, 0.52f, 0.08,
            ModelKind.SWARM_TINY, 0.52f, 0.82f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.MELEE_BRUISER, Behavior2.FAST_SWARM)),

    WHISPER_MOTE("spirit2_whisper_mote", "Whisper Mote", 1, 12, 2, 0.38, 0.44f, 0,
            ModelKind.SWARM_TINY, 0.44f, 0.68f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.VOICE_CURSE, Behavior2.TELEPORT)),

    GRAVE_FLEA("spirit2_grave_flea", "Grave Flea", 1, 15, 3, 0.32, 0.5f, 0.05,
            ModelKind.CORPSE, 0.5f, 0.85f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.NECROMANCY_RISE, Behavior2.WITHER_TOUCH)),

    BILE_GNAT("spirit2_bile_gnat", "Bile Gnat", 1, 14, 3, 0.34, 0.47f, 0,
            ModelKind.SWARM_TINY, 0.47f, 0.74f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.POISON_AURA)),

    SHADE_CRAWLER("spirit2_shade_crawler", "Shade Crawler", 1, 13, 3, 0.35, 0.46f, 0,
            ModelKind.GAUNT, 0.46f, 1f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.TELEPORT, Behavior2.BLINDNESS_CURSE)),

    CURSE_TICK("spirit2_curse_tick", "Curse Tick", 1, 16, 3, 0.29, 0.51f, 0.05,
            ModelKind.SWARM_TINY, 0.51f, 0.81f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.LIFE_DRAIN)),

    VOID_MOTE("spirit2_void_mote", "Void Mote", 1, 12, 3, 0.37, 0.44f, 0,
            ModelKind.SWARM_TINY, 0.44f, 0.68f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.GRAVITY_PULL)),

    HUSK_FLEA("spirit2_husk_flea", "Husk Flea", 1, 15, 3, 0.3, 0.5f, 0.05,
            ModelKind.CORPSE, 0.5f, 0.82f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.SWARM_REPLICATE)),

    SEEP_LARVA("spirit2_seep_larva", "Seep Larva", 1, 14, 2, 0.33, 0.48f, 0,
            ModelKind.SWARM_TINY, 0.48f, 0.76f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior2.FROST_AURA, Behavior2.POISON_AURA)),

    SLIT_WHISPERER("spirit2_slit_whisperer", "Slit Whisperer", 2, 24, 5, 0.28, 0.95f, 0.1,
            ModelKind.HORNED, 0.65f, 2.1f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.WITHER_TOUCH, Behavior2.VOICE_CURSE)),

    BRICK_BRUTE("spirit2_brick_brute", "Brick Brute", 2, 32, 7, 0.23, 1.05f, 0.25,
            ModelKind.HULKING, 0.85f, 2.4f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.MELEE_BRUISER)),

    VENOM_SPITTER("spirit2_venom_spitter", "Venom Spitter", 2, 22, 5, 0.26, 0.9f, 0.1,
            ModelKind.GAUNT, 0.65f, 2.1f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.RANGED_BLAST, Behavior2.POISON_AURA)),

    HEX_WEAVER("spirit2_hex_weaver", "Hex Weaver", 2, 25, 5, 0.27, 0.95f, 0.1,
            ModelKind.HORNED, 0.68f, 2.15f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.FROST_AURA, Behavior2.CURSE_SEAL)),

    MIRROR_CURSE("spirit2_mirror_curse", "Mirror Curse", 2, 26, 6, 0.29, 0.92f, 0.1,
            ModelKind.GAUNT, 0.62f, 2f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.TELEPORT, Behavior2.RANGED_BLAST)),

    CHAIN_HANGER("spirit2_chain_hanger", "Chain Hanger", 2, 28, 6, 0.25, 1f, 0.15,
            ModelKind.HULKING, 0.75f, 2.3f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.GRAVITY_PULL, Behavior2.CURSE_SEAL)),

    BELL_RINGER("spirit2_bell_ringer", "Bell Ringer", 2, 27, 5, 0.26, 0.98f, 0.12,
            ModelKind.HORNED, 0.72f, 2.25f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.SHOCKWAVE, Behavior2.VOICE_CURSE)),

    PALE_STALKER("spirit2_pale_stalker", "Pale Stalker", 2, 24, 6, 0.3, 0.9f, 0.08,
            ModelKind.GAUNT, 0.6f, 2f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.TELEPORT, Behavior2.BLINDNESS_CURSE)),

    ROOT_CRAWLER("spirit2_root_crawler", "Root Crawler", 2, 30, 6, 0.22, 1f, 0.2,
            ModelKind.HULKING, 0.8f, 2.35f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.FROST_AURA, Behavior2.REGEN)),

    NEEDLE_FACE("spirit2_needle_face", "Needle Face", 2, 23, 7, 0.28, 0.88f, 0.1,
            ModelKind.GAUNT, 0.58f, 1.95f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.RANGED_BLAST, Behavior2.WITHER_TOUCH)),

    COFFIN_CRAWLER("spirit2_coffin_crawler", "Coffin Crawler", 2, 31, 6, 0.24, 1.02f, 0.18,
            ModelKind.CORPSE, 0.82f, 2.3f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.NECROMANCY_RISE)),

    BLOOD_WEEPER("spirit2_blood_weeper", "Blood Weeper", 2, 26, 6, 0.27, 0.94f, 0.12,
            ModelKind.GAUNT, 0.66f, 2.1f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.LIFE_DRAIN, Behavior2.POISON_AURA)),

    SHRINE_HAUNTER("spirit2_shrine_haunter", "Shrine Haunter", 2, 28, 5, 0.26, 0.96f, 0.12,
            ModelKind.HORNED, 0.7f, 2.2f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.DOMAIN_FIELD)),

    LEDGER_CURSE("spirit2_ledger_curse", "Ledger Curse", 2, 25, 5, 0.27, 0.93f, 0.1,
            ModelKind.TOOL, 0.64f, 2.05f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.CURSE_SEAL, Behavior2.RANGED_BLAST)),

    KNIFE_WHISPER("spirit2_knife_whisper", "Knife Whisper", 2, 22, 8, 0.31, 0.85f, 0.08,
            ModelKind.TOOL, 0.58f, 1.9f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior2.ARMAMENT_FORM, Behavior2.MELEE_BRUISER)),

    HORNED_DEVOURER("spirit2_horned_devourer", "Horned Devourer", 3, 42, 9, 0.27, 1.15f, 0.3,
            ModelKind.HORNED, 0.9f, 2.7f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.MELEE_BRUISER, Behavior2.LIFE_DRAIN)),

    VEIL_STALKER("spirit2_veil_stalker", "Veil Stalker", 3, 38, 8, 0.3, 1.05f, 0.2,
            ModelKind.GAUNT, 0.78f, 2.5f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.TELEPORT, Behavior2.BLINDNESS_CURSE)),

    PLAGUE_HARBINGER("spirit2_plague_harbinger", "Plague Harbinger", 3, 44, 8, 0.25, 1.18f, 0.3,
            ModelKind.HULKING, 0.95f, 2.65f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.POISON_AURA, Behavior2.REGEN, Behavior2.CHAIN_CURSE)),

    EMBER_REAVER("spirit2_ember_reaver", "Ember Reaver", 3, 40, 10, 0.27, 1.1f, 0.28,
            ModelKind.HORNED, 0.85f, 2.55f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.FIRE_BLAST, Behavior2.FIRE_IMMUNE, Behavior2.SHOCKWAVE)),

    BONE_COLLECTOR("spirit2_bone_collector", "Bone Collector", 3, 46, 9, 0.24, 1.2f, 0.35,
            ModelKind.CORPSE, 0.98f, 2.7f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.NECROMANCY_RISE, Behavior2.SUMMONER)),

    FLOOD_WRAITH("spirit2_flood_wraith", "Flood Wraith", 3, 39, 9, 0.28, 1.08f, 0.22,
            ModelKind.GAUNT, 0.8f, 2.5f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.RANGED_BLAST, Behavior2.GRAVITY_PULL)),

    GRAVE_KNIGHT_CURSE("spirit2_grave_knight_curse", "Grave Knight Curse", 3, 48, 10, 0.26, 1.22f, 0.4,
            ModelKind.HULKING, 1f, 2.8f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.MELEE_BRUISER, Behavior2.SHOCKWAVE)),

    SHIKIGAMI_FOX("spirit2_shikigami_fox", "Shikigami Fox", 3, 36, 8, 0.32, 1f, 0.15,
            ModelKind.SERPENT, 0.75f, 2.2f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.SHIKIGAMI_CALL, Behavior2.TELEPORT)),

    SHIKIGAMI_OWL("spirit2_shikigami_owl", "Shikigami Owl", 3, 37, 8, 0.31, 1.02f, 0.18,
            ModelKind.WINGED, 0.78f, 2.3f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.SHIKIGAMI_CALL, Behavior2.RANGED_BLAST)),

    DOMAIN_LESSER("spirit2_domain_lesser", "Lesser Domain Spirit", 3, 41, 9, 0.26, 1.12f, 0.25,
            ModelKind.HORNED, 0.88f, 2.6f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.DOMAIN_FIELD, Behavior2.CURSE_SEAL)),

    TOOL_REVENANT("spirit2_tool_revenant", "Tool Revenant", 3, 43, 10, 0.27, 1.1f, 0.28,
            ModelKind.TOOL, 0.86f, 2.55f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.ARMAMENT_FORM, Behavior2.RANGED_BLAST)),

    CORPSE_PUPPETEER("spirit2_corpse_puppeteer", "Corpse Puppeteer", 3, 45, 8, 0.25, 1.15f, 0.3,
            ModelKind.CORPSE, 0.92f, 2.65f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.NECROMANCY_RISE, Behavior2.SUMMONER)),

    CURSE_ARMAMENT("spirit2_curse_armament", "Curse Armament", 3, 40, 11, 0.28, 1.08f, 0.25,
            ModelKind.TOOL, 0.84f, 2.5f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.ARMAMENT_FORM, Behavior2.MELEE_BRUISER, Behavior2.SHOCKWAVE)),

    DISASTER_SEED("spirit2_disaster_seed", "Disaster Seed", 3, 42, 9, 0.26, 1.14f, 0.28,
            ModelKind.HULKING, 0.9f, 2.6f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.DISASTER_ERUPT)),

    MAX_ECHO("spirit2_max_echo", "Maximum Echo", 3, 38, 10, 0.29, 1.06f, 0.22,
            ModelKind.GAUNT, 0.82f, 2.45f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior2.MAXIMUM_BURST)),

    VOLCANO_CALAMITY("spirit2_volcano_calamity", "Volcano Calamity", 4, 75, 12, 0.26, 1.35f, 0.5,
            ModelKind.HULKING, 1.15f, 3f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.DISASTER_ERUPT, Behavior2.FIRE_BLAST, Behavior2.FIRE_IMMUNE, Behavior2.SHOCKWAVE)),

    FOREST_CALAMITY("spirit2_forest_calamity", "Forest Calamity", 4, 82, 11, 0.24, 1.4f, 0.55,
            ModelKind.HULKING, 1.2f, 3.05f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.FROST_AURA, Behavior2.REGEN, Behavior2.SUMMONER, Behavior2.DOMAIN_FIELD)),

    OCEAN_CALAMITY("spirit2_ocean_calamity", "Ocean Calamity", 4, 80, 12, 0.26, 1.38f, 0.5,
            ModelKind.HULKING, 1.18f, 3.1f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.RANGED_BLAST, Behavior2.GRAVITY_PULL, Behavior2.SUMMONER)),

    PLAGUE_CALAMITY("spirit2_plague_calamity", "Plague Calamity", 4, 78, 11, 0.25, 1.36f, 0.52,
            ModelKind.HULKING, 1.16f, 3f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.POISON_AURA, Behavior2.CHAIN_CURSE, Behavior2.REGEN)),

    CURSED_WOMB_SPAWN("spirit2_cursed_womb_spawn", "Womb Spawn", 4, 70, 13, 0.28, 1.3f, 0.45,
            ModelKind.CORPSE, 1.05f, 2.9f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.SUMMONER, Behavior2.SWARM_REPLICATE, Behavior2.LIFE_DRAIN)),

    TRANSFIGURED_HORDE("spirit2_transfigured_horde", "Transfigured Horde", 4, 74, 13, 0.3, 1.28f, 0.48,
            ModelKind.GAUNT, 1f, 2.85f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.WITHER_TOUCH, Behavior2.LIFE_DRAIN, Behavior2.TELEPORT)),

    CORPSE_HORDE("spirit2_corpse_horde", "Corpse Horde", 4, 76, 12, 0.24, 1.34f, 0.5,
            ModelKind.CORPSE, 1.12f, 3f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.NECROMANCY_RISE, Behavior2.SUMMONER)),

    DOMAIN_STORM("spirit2_domain_storm", "Domain Storm", 4, 72, 13, 0.29, 1.32f, 0.46,
            ModelKind.HORNED, 1.08f, 2.95f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior2.DOMAIN_FIELD, Behavior2.MAXIMUM_BURST, Behavior2.SHOCKWAVE)),

    FINGER_BEARER_ALPHA("spirit2_finger_bearer_alpha", "Finger Bearer Alpha", 5, 210, 19, 0.28, 1.55f, 0.65,
            ModelKind.HORNED, 1.5f, 4f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.MELEE_BRUISER, Behavior2.SHOCKWAVE, Behavior2.ENRAGE, Behavior2.SUMMONER)),

    FINGER_BEARER_OMEGA("spirit2_finger_bearer_omega", "Finger Bearer Omega", 5, 235, 21, 0.3, 1.65f, 0.7,
            ModelKind.HORNED, 1.6f, 4.2f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.MELEE_BRUISER, Behavior2.SHOCKWAVE, Behavior2.ENRAGE, Behavior2.MAXIMUM_BURST)),

    SMALLPOX_DEITY("spirit2_smallpox_deity", "Smallpox Deity", 5, 280, 17, 0.24, 1.75f, 0.75,
            ModelKind.HULKING, 1.7f, 4.5f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.POISON_AURA, Behavior2.CHAIN_CURSE, Behavior2.REGEN, Behavior2.DOMAIN_FIELD, Behavior2.ENRAGE)),

    CURSED_WOMB("spirit2_cursed_womb", "Cursed Womb", 5, 300, 16, 0.22, 1.9f, 0.8,
            ModelKind.CORPSE, 1.85f, 4.8f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.SUMMONER, Behavior2.SWARM_REPLICATE, Behavior2.LIFE_DRAIN, Behavior2.ENRAGE)),

    DRAGON_CURSE("spirit2_dragon_curse", "Dragon Curse", 5, 320, 22, 0.26, 2f, 0.85,
            ModelKind.WINGED, 2f, 4.6f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.FIRE_BLAST, Behavior2.FIRE_IMMUNE, Behavior2.SHOCKWAVE, Behavior2.RANGED_BLAST, Behavior2.ENRAGE)),

    RAINBOW_DRAGON("spirit2_rainbow_dragon", "Rainbow Dragon Curse", 5, 340, 20, 0.28, 2.1f, 0.8,
            ModelKind.WINGED, 2.1f, 4.8f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.RAINBOW_BEAM, Behavior2.TELEPORT, Behavior2.SHOCKWAVE, Behavior2.ENRAGE)),

    DISASTER_VOLCANO_LORD("spirit2_disaster_volcano_lord", "Volcano Disaster Lord", 5, 270, 20, 0.25, 1.8f, 0.78,
            ModelKind.HULKING, 1.75f, 4.5f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.DISASTER_ERUPT, Behavior2.FIRE_BLAST, Behavior2.FIRE_IMMUNE, Behavior2.DOMAIN_FIELD, Behavior2.ENRAGE)),

    DISASTER_FOREST_SOVEREIGN("spirit2_disaster_forest_sovereign", "Forest Disaster Sovereign", 5, 265, 18, 0.24, 1.85f, 0.75,
            ModelKind.HULKING, 1.8f, 4.6f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.FROST_AURA, Behavior2.REGEN, Behavior2.SUMMONER, Behavior2.DOMAIN_FIELD, Behavior2.ENRAGE)),

    DISASTER_OCEAN_TYRANT("spirit2_disaster_ocean_tyrant", "Ocean Disaster Tyrant", 5, 275, 19, 0.26, 1.82f, 0.76,
            ModelKind.HULKING, 1.78f, 4.55f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.RANGED_BLAST, Behavior2.GRAVITY_PULL, Behavior2.SUMMONER, Behavior2.SHOCKWAVE, Behavior2.ENRAGE)),

    DISASTER_PLAGUE_KING("spirit2_disaster_plague_king", "Plague Disaster King", 5, 260, 17, 0.23, 1.78f, 0.74,
            ModelKind.HULKING, 1.72f, 4.4f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.POISON_AURA, Behavior2.CHAIN_CURSE, Behavior2.REGEN, Behavior2.SUMMONER, Behavior2.ENRAGE)),

    DOMAIN_EMPEROR("spirit2_domain_emperor", "Domain Emperor", 5, 290, 18, 0.27, 1.7f, 0.72,
            ModelKind.HORNED, 1.65f, 4.3f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.DOMAIN_FIELD, Behavior2.CURSE_SEAL, Behavior2.MAXIMUM_BURST, Behavior2.ENRAGE)),

    MAXIMUM_TECHNIQUE_ECHO("spirit2_maximum_technique_echo", "Maximum Technique Echo", 5, 250, 24, 0.3, 1.6f, 0.68,
            ModelKind.GAUNT, 1.45f, 4f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.MAXIMUM_BURST, Behavior2.SHOCKWAVE, Behavior2.TELEPORT, Behavior2.ENRAGE)),

    SPECIAL_GRAVE_SOVEREIGN("spirit2_special_grave_sovereign", "Grave Sovereign", 5, 255, 19, 0.25, 1.75f, 0.76,
            ModelKind.CORPSE, 1.7f, 4.5f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.NECROMANCY_RISE, Behavior2.SUMMONER, Behavior2.SHOCKWAVE, Behavior2.ENRAGE)),

    CURSED_TOOL_MASTERY("spirit2_cursed_tool_mastery", "Cursed Tool Mastery", 5, 245, 22, 0.29, 1.55f, 0.65,
            ModelKind.TOOL, 1.5f, 4f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.ARMAMENT_FORM, Behavior2.MELEE_BRUISER, Behavior2.RANGED_BLAST, Behavior2.ENRAGE)),

    SHIKIGAMI_LORD("spirit2_shikigami_lord", "Shikigami Lord", 5, 230, 18, 0.31, 1.5f, 0.6,
            ModelKind.WINGED, 1.45f, 3.9f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.SHIKIGAMI_CALL, Behavior2.SUMMONER, Behavior2.TELEPORT, Behavior2.ENRAGE)),

    NINE_TAILED_SHADOW("spirit2_nine_tailed_shadow", "Nine-Tailed Shadow", 5, 240, 20, 0.32, 1.58f, 0.62,
            ModelKind.SERPENT, 1.52f, 4.1f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.SHIKIGAMI_CALL, Behavior2.RANGED_BLAST, Behavior2.TELEPORT, Behavior2.ENRAGE)),

    CORPSE_EMPEROR("spirit2_corpse_emperor", "Corpse Emperor", 5, 285, 17, 0.23, 1.88f, 0.78,
            ModelKind.CORPSE, 1.82f, 4.7f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.NECROMANCY_RISE, Behavior2.SUMMONER, Behavior2.DOMAIN_FIELD, Behavior2.ENRAGE)),

    RAINBOW_SOVEREIGN("spirit2_rainbow_sovereign", "Rainbow Sovereign", 5, 310, 21, 0.29, 1.95f, 0.82,
            ModelKind.WINGED, 1.95f, 4.7f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.RAINBOW_BEAM, Behavior2.MAXIMUM_BURST, Behavior2.TELEPORT, Behavior2.ENRAGE)),

    CURSED_KING_VESSEL("spirit2_cursed_king_vessel", "Cursed King Vessel", 5, 295, 23, 0.28, 1.72f, 0.74,
            ModelKind.HORNED, 1.68f, 4.4f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.MELEE_BRUISER, Behavior2.MAXIMUM_BURST, Behavior2.DOMAIN_FIELD, Behavior2.ENRAGE)),

    CATACLYSM_ARCHON("spirit2_cataclysm_archon", "Cataclysm Archon", 5, 305, 20, 0.27, 1.8f, 0.8,
            ModelKind.HORNED, 1.75f, 4.5f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior2.DISASTER_ERUPT, Behavior2.SHOCKWAVE, Behavior2.FIRE_BLAST, Behavior2.TELEPORT, Behavior2.ENRAGE));

    /** Client model archetype (humanoid rig variants). */
    public enum ModelKind { GAUNT, SWARM_TINY, HULKING, HORNED, WINGED, SERPENT, TOOL, CORPSE }

    public static final int COUNT = 90;

    private final String id;
    private final String displayName;
    private final int gradeBand;
    private final double baseHealth;
    private final double baseDamage;
    private final double moveSpeed;
    private final float baseScale;
    private final double knockbackResist;
    private final ModelKind modelKind;
    private final float hitboxWidth;
    private final float hitboxHeight;
    private final ChatFormatting nameColor;
    private final int lootTier;
    private final boolean boss;
    private final Set<Behavior2> behaviors;

    SpiritSpecies2(String id, String displayName, int gradeBand, double baseHealth, double baseDamage,
                   double moveSpeed, float baseScale, double knockbackResist, ModelKind modelKind,
                   float hitboxWidth, float hitboxHeight, ChatFormatting nameColor, int lootTier,
                   boolean boss, Set<Behavior2> behaviors) {
        this.id = id;
        this.displayName = displayName;
        this.gradeBand = gradeBand;
        this.baseHealth = baseHealth;
        this.baseDamage = baseDamage;
        this.moveSpeed = moveSpeed;
        this.baseScale = baseScale;
        this.knockbackResist = knockbackResist;
        this.modelKind = modelKind;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.nameColor = nameColor;
        this.lootTier = lootTier;
        this.boss = boss;
        this.behaviors = behaviors;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public int gradeBand() { return gradeBand; }
    public double baseHealth() { return baseHealth; }
    public double baseDamage() { return baseDamage; }
    public double moveSpeed() { return moveSpeed; }
    public float baseScale() { return baseScale; }
    public double knockbackResist() { return knockbackResist; }
    public ModelKind modelKind() { return modelKind; }
    public float hitboxWidth() { return hitboxWidth; }
    public float hitboxHeight() { return hitboxHeight; }
    public ChatFormatting nameColor() { return nameColor; }
    public int lootTier() { return lootTier; }
    public boolean boss() { return boss; }

    public boolean has(Behavior2 b) { return behaviors.contains(b); }

    public static SpiritSpecies2 byId(String id) {
        for (SpiritSpecies2 s : values()) if (s.id.equalsIgnoreCase(id)) return s;
        return null;
    }
}
