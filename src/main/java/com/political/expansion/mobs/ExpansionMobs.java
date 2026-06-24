package com.political.expansion.mobs;

import com.political.items.RpgItem;
import com.political.items.RpgItems;
import com.political.politics.DataManager;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Central registry for the non-curse RPG creature set. Mirrors the codebase pattern in
 * {@code com.political.curse.ModEntities}: every {@link MobSpec} gets an {@link EntityType},
 * default attributes via {@link FabricDefaultAttributeRegistry}, a natural-spawn entry via the
 * Fabric biome API, and a death reward hook (coins + custom drops) via a server event. No mixins.
 *
 * <p>Integration must call {@link #register()} from the common initializer, register
 * {@link MobCommands} in the command callback, and call the client renderer registrar
 * {@code com.political.expansion.mobs.ExpansionMobsClient#registerClient()} from the client init.
 */
public final class ExpansionMobs {

    public static final String MOD_ID = "politicalserver";

    /** Ordered list of every creature spec (stable for client renderer iteration). */
    public static final List<MobSpec> SPECS = new ArrayList<>();

    private static final Map<String, MobSpec> BY_ID = new HashMap<>();
    private static final Map<EntityType<?>, MobSpec> BY_TYPE = new HashMap<>();
    private static final Random RNG = new Random();

    /** Soft toggle for natural spawning (commands can flip it); affects newly spawned mobs only. */
    public static boolean naturalSpawnsEnabled = true;

    private static boolean bootstrapped = false;

    private ExpansionMobs() {}

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    public static void register() {
        bootstrap();
        for (MobSpec spec : SPECS) registerType(spec);
        registerSpawns();
        registerDeathRewards();
    }

    public static MobSpec specForType(EntityType<?> type) {
        return BY_TYPE.get(type);
    }

    public static MobSpec specById(String id) {
        return BY_ID.get(id);
    }

    public static List<String> ids() {
        List<String> out = new ArrayList<>(SPECS.size());
        for (MobSpec s : SPECS) out.add(s.id);
        return out;
    }

    /** Spawns a creature by id at a world position. Used by the command + boss summoning. */
    public static ExpansionMob spawnById(ServerLevel level, BlockPos pos, String id) {
        MobSpec spec = BY_ID.get(id);
        if (spec == null || spec.type == null) return null;
        ExpansionMob mob = spec.type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return null;
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(mob);
        return mob;
    }

    /** Spawns a phase-2 minion near a boss. */
    static void summonAdd(ServerLevel level, ExpansionMob boss, String id) {
        MobSpec spec = BY_ID.get(id);
        if (spec == null || spec.type == null) return;
        ExpansionMob mob = spec.type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos(boss.getX() + (RNG.nextDouble() - 0.5) * 4.0,
                boss.getY(),
                boss.getZ() + (RNG.nextDouble() - 0.5) * 4.0);
        level.addFreshEntity(mob);
    }

    // ------------------------------------------------------------------
    // Registration internals
    // ------------------------------------------------------------------

    private static void registerType(MobSpec spec) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, spec.id));
        EntityType.Builder<ExpansionMob> builder = EntityType.Builder
                .<ExpansionMob>of(ExpansionMob::new, spec.category)
                .sized(spec.width, spec.height);
        if (spec.fireImmune) builder = builder.fireImmune();
        EntityType<ExpansionMob> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
        FabricDefaultAttributeRegistry.register(type, ExpansionMob.createAttributes(spec));
        spec.type = type;
        BY_TYPE.put(type, spec);
    }

    private static void registerSpawns() {
        for (MobSpec spec : SPECS) {
            if (spec.type == null) continue;
            // Ground placement rules (standard monster darkness/ground checks) for every creature.
            SpawnPlacements.register(spec.type, SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ExpansionMob::checkSpawnRules);
            if (spec.spawnWeight > 0) {
                if (spec.role.isBossLike() && !com.political.config.PoliticalConfig.get().minibossNaturalSpawnsEnabled) {
                    continue;
                }
                if (spec.role == MobRole.BOSS) continue;
                int weight = com.political.config.PoliticalConfig.get().scaleSpawnWeight(spec.spawnWeight);
                BiomeModifications.addSpawn(spec.biomeSelector, MobCategory.MONSTER, spec.type,
                        weight, spec.minGroup, spec.maxGroup);
            }
        }
    }

    private static void registerDeathRewards() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ExpansionMob mob)) return;
            MobSpec spec = mob.spec();
            if (spec == null || !(entity.level() instanceof ServerLevel level)) return;

            if (source.getEntity() instanceof ServerPlayer killer) {
                int coins = spec.coinMax > spec.coinMin
                        ? spec.coinMin + RNG.nextInt(spec.coinMax - spec.coinMin + 1)
                        : spec.coinMin;
                coins = com.political.config.PoliticalConfig.get().scaleCoins(coins);
                if (coins > 0) {
                    DataManager.addCoins(killer.getStringUUID(), coins);
                    if (spec.role.isBossLike()) {
                        killer.sendSystemMessage(Component.literal("Slew " + spec.name + "! +" + coins + " coins.")
                                .withStyle(ChatFormatting.GOLD));
                    }
                }
            }

            for (MobSpec.Drop drop : spec.drops) {
                if (RNG.nextFloat() >= drop.chance) continue;
                ItemStack stack = drop.factory.get();
                if (stack == null || stack.isEmpty()) continue;
                level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                        level, mob.getX(), mob.getY() + 0.5, mob.getZ(), stack));
            }
        });
    }

    // ------------------------------------------------------------------
    // Spec helpers
    // ------------------------------------------------------------------

    private static Supplier<ItemStack> stack(Item item, int min, int max) {
        return () -> new ItemStack(item, max > min ? min + RNG.nextInt(max - min + 1) : min);
    }

    private static Supplier<ItemStack> rpg(RpgItem def) {
        return () -> RpgItems.create(def);
    }

    private static Predicate<BiomeSelectionContext> overworld() {
        return BiomeSelectors.foundInOverworld();
    }

    private static Predicate<BiomeSelectionContext> inTag(TagKey<Biome> tag) {
        return BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(tag));
    }

    private static MobSpec add(MobSpec spec) {
        SPECS.add(spec);
        BY_ID.put(spec.id, spec);
        return spec;
    }

    // ------------------------------------------------------------------
    // The roster
    // ------------------------------------------------------------------

    private static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        // ---- Hostile overworld monsters ----------------------------------
        add(MobSpec.of("mob_ashen_knight", "Ashen Knight", MobRole.HOSTILE).brute().raidsVillages()
                .stats(40, 7, 8, 0.23).resist(0.6, 36).size(0.7f, 2.1f, 1.05f)
                .coins(3, 8)
                .drop(0.8f, stack(Items.BONE, 1, 2))
                .drop(0.3f, stack(Items.IRON_INGOT, 1, 1))
                .drop(0.5f, stack(Items.ROTTEN_FLESH, 1, 2))
                .spawn(overworld(), 10, 1, 2));

        add(MobSpec.of("mob_bandit_outlaw", "Bandit Outlaw", MobRole.HOSTILE).raidsVillages()
                .stats(26, 5, 2, 0.30).resist(0.0, 32).size(0.6f, 1.95f, 1.0f)
                .coins(5, 12)
                .drop(0.3f, stack(Items.EMERALD, 1, 1))
                .drop(0.5f, stack(Items.LEATHER, 1, 2))
                .drop(0.15f, stack(Items.IRON_INGOT, 1, 1))
                .spawn(inTag(BiomeTags.IS_SAVANNA), 10, 2, 3));

        add(MobSpec.of("mob_bandit_brute", "Bandit Brute", MobRole.HOSTILE).brute().raidsVillages()
                .stats(40, 8, 4, 0.26).resist(0.3, 34).size(0.8f, 2.2f, 1.1f)
                .coins(6, 14)
                .drop(0.4f, stack(Items.IRON_INGOT, 1, 2))
                .drop(0.25f, stack(Items.EMERALD, 1, 1))
                .spawn(inTag(BiomeTags.IS_FOREST), 8, 1, 2));

        add(MobSpec.of("mob_grave_revenant", "Grave Revenant", MobRole.HOSTILE)
                .stats(34, 6, 4, 0.24).resist(0.1, 34).size(0.7f, 2.0f, 1.0f)
                .aura(MobEffects.WEAKNESS, 0)
                .coins(4, 9)
                .drop(0.8f, stack(Items.ROTTEN_FLESH, 1, 2))
                .drop(0.5f, stack(Items.BONE, 1, 1))
                .drop(0.2f, stack(Items.REDSTONE, 1, 2))
                .spawn(overworld(), 8, 1, 2));

        add(MobSpec.of("mob_ember_fiend", "Ember Fiend", MobRole.HOSTILE).fireproof().ignites()
                .stats(28, 7, 2, 0.30).resist(0.1, 32).size(0.7f, 2.0f, 1.05f)
                .coins(5, 11)
                .drop(0.4f, stack(Items.MAGMA_CREAM, 1, 2))
                .drop(0.6f, stack(Items.COAL, 1, 2))
                .spawn(inTag(BiomeTags.IS_BADLANDS), 8, 1, 2));

        add(MobSpec.of("mob_frost_revenant", "Frost Revenant", MobRole.HOSTILE)
                .stats(30, 6, 4, 0.24).resist(0.2, 34).size(0.7f, 2.05f, 1.05f)
                .onHit(MobEffects.SLOWNESS, 80, 0)
                .coins(5, 11)
                .drop(0.4f, stack(Items.PACKED_ICE, 1, 1))
                .drop(0.3f, stack(Items.PRISMARINE_SHARD, 1, 1))
                .spawn(inTag(BiomeTags.IS_TAIGA), 9, 1, 2));

        add(MobSpec.of("mob_storm_herald", "Storm Herald", MobRole.HOSTILE).lightning()
                .stats(30, 6, 3, 0.28).resist(0.1, 36).size(0.7f, 2.05f, 1.05f)
                .coins(6, 13)
                .drop(0.6f, stack(Items.COPPER_INGOT, 1, 2))
                .spawn(inTag(BiomeTags.IS_MOUNTAIN), 6, 1, 1));

        add(MobSpec.of("mob_venom_cultist", "Venom Cultist", MobRole.HOSTILE)
                .stats(26, 5, 2, 0.30).resist(0.0, 32).size(0.6f, 1.95f, 1.0f)
                .onHit(MobEffects.POISON, 100, 0)
                .coins(5, 11)
                .drop(0.5f, stack(Items.SPIDER_EYE, 1, 2))
                .drop(0.3f, stack(Items.SLIME_BALL, 1, 1))
                .spawn(inTag(BiomeTags.IS_JUNGLE), 8, 1, 2));

        add(MobSpec.of("mob_bone_legionnaire", "Bone Legionnaire", MobRole.HOSTILE).raidsVillages()
                .stats(28, 6, 5, 0.26).resist(0.2, 34).size(0.6f, 1.99f, 1.0f)
                .coins(4, 9)
                .drop(0.9f, stack(Items.BONE, 1, 3))
                .drop(0.4f, stack(Items.ARROW, 1, 2))
                .spawn(overworld(), 10, 1, 3));

        add(MobSpec.of("mob_wraith", "Wraith", MobRole.HOSTILE)
                .stats(22, 7, 0, 0.36).resist(0.0, 36).size(0.6f, 2.0f, 1.0f)
                .onHit(MobEffects.BLINDNESS, 60, 0)
                .coins(5, 12)
                .drop(0.3f, stack(Items.GLOWSTONE_DUST, 1, 1))
                .drop(0.1f, stack(Items.ENDER_PEARL, 1, 1))
                .spawn(overworld(), 7, 1, 2));

        add(MobSpec.of("mob_plague_bearer", "Plague Bearer", MobRole.HOSTILE)
                .stats(30, 5, 3, 0.24).resist(0.1, 32).size(0.7f, 2.0f, 1.05f)
                .onHit(MobEffects.HUNGER, 120, 0)
                .coins(4, 9)
                .drop(0.9f, stack(Items.ROTTEN_FLESH, 1, 3))
                .drop(0.2f, stack(Items.POISONOUS_POTATO, 1, 1))
                .spawn(overworld(), 8, 1, 2));

        add(MobSpec.of("mob_cultist_acolyte", "Cultist Acolyte", MobRole.HOSTILE)
                .stats(24, 5, 2, 0.28).resist(0.0, 34).size(0.6f, 1.95f, 1.0f)
                .aura(MobEffects.WEAKNESS, 0)
                .coins(6, 13)
                .drop(0.5f, stack(Items.REDSTONE, 1, 2))
                .drop(0.3f, stack(Items.AMETHYST_SHARD, 1, 1))
                .spawn(inTag(BiomeTags.IS_FOREST), 6, 1, 2));

        add(MobSpec.of("mob_gnoll_raider", "Gnoll Raider", MobRole.HOSTILE).raidsVillages()
                .stats(20, 5, 1, 0.33).resist(0.0, 32).size(0.6f, 1.8f, 0.95f)
                .coins(3, 7)
                .drop(0.6f, stack(Items.LEATHER, 1, 2))
                .drop(0.4f, stack(Items.BONE, 1, 1))
                .spawn(inTag(BiomeTags.IS_SAVANNA), 10, 3, 4));

        // ---- Neutral / wild creatures (nocturnal, retaliate only) --------
        add(MobSpec.of("mob_forest_troll", "Forest Troll", MobRole.NEUTRAL).brute()
                .stats(50, 9, 6, 0.22).resist(0.5, 34).size(1.0f, 2.5f, 1.25f)
                .lifesteal(2.0f)
                .coins(4, 10)
                .drop(0.6f, stack(Items.OAK_LOG, 1, 3))
                .drop(0.5f, stack(Items.LEATHER, 1, 2))
                .spawn(inTag(BiomeTags.IS_JUNGLE), 6, 1, 1));

        add(MobSpec.of("mob_stone_sentinel", "Stone Sentinel", MobRole.NEUTRAL).brute()
                .stats(60, 8, 10, 0.18).resist(0.8, 34).size(0.9f, 2.5f, 1.2f)
                .coins(5, 12)
                .drop(0.5f, stack(Items.IRON_INGOT, 1, 2))
                .drop(0.8f, stack(Items.COBBLESTONE, 2, 4))
                .spawn(inTag(BiomeTags.IS_MOUNTAIN), 5, 1, 1));

        add(MobSpec.of("mob_marsh_lurker", "Marsh Lurker", MobRole.NEUTRAL)
                .stats(30, 5, 2, 0.26).resist(0.1, 32).size(0.7f, 1.95f, 1.0f)
                .onHit(MobEffects.SLOWNESS, 60, 0)
                .coins(3, 8)
                .drop(0.5f, stack(Items.SLIME_BALL, 1, 2))
                .drop(0.4f, stack(Items.SEAGRASS, 1, 2))
                .spawn(overworld(), 6, 1, 2));

        add(MobSpec.of("mob_crystal_warden", "Crystal Warden", MobRole.SKITTISH)
                .stats(24, 4, 3, 0.30).resist(0.0, 32).size(0.6f, 1.9f, 1.0f)
                .coins(4, 10)
                .drop(0.6f, stack(Items.AMETHYST_SHARD, 1, 2))
                .drop(0.3f, stack(Items.QUARTZ, 1, 1))
                .spawn(inTag(BiomeTags.IS_JUNGLE), 5, 1, 2));

        // ---- Mini-bosses (rare, boss bar) --------------------------------
        add(MobSpec.of("mob_ironclad_champion", "Ironclad Champion", MobRole.MINIBOSS).brute().raidsVillages()
                .stats(120, 14, 12, 0.26).resist(0.7, 40).size(0.9f, 2.4f, 1.2f)
                .coins(40, 80)
                .drop(0.8f, stack(Items.IRON_BLOCK, 1, 1))
                .drop(0.5f, stack(Items.DIAMOND, 1, 2))
                .drop(0.1f, rpg(RpgItem.RADIANT_HALBERD))
                .spawn(inTag(BiomeTags.IS_FOREST), 1, 1, 1));

        add(MobSpec.of("mob_blighted_ogre", "Blighted Ogre", MobRole.MINIBOSS).brute()
                .stats(140, 16, 8, 0.24).resist(0.6, 40).size(1.1f, 2.7f, 1.35f)
                .onHit(MobEffects.HUNGER, 120, 0).aura(MobEffects.WEAKNESS, 0)
                .coins(50, 90)
                .drop(0.5f, stack(Items.SLIME_BLOCK, 1, 1))
                .drop(0.7f, stack(Items.EMERALD, 2, 4))
                .spawn(overworld(), 1, 1, 1));

        add(MobSpec.of("mob_frostking_sentinel", "Frostking Sentinel", MobRole.MINIBOSS).brute()
                .stats(130, 14, 10, 0.22).resist(0.7, 40).size(0.9f, 2.5f, 1.25f)
                .onHit(MobEffects.SLOWNESS, 100, 1)
                .coins(45, 85)
                .drop(0.8f, stack(Items.PACKED_ICE, 2, 4))
                .drop(0.2f, stack(Items.DIAMOND, 1, 1))
                .spawn(inTag(BiomeTags.IS_TAIGA), 1, 1, 1));

        // ---- Bosses (phases, boss bar, summon-only) ----------------------
        add(MobSpec.of("mob_warlord_kael", "Warlord Kael", MobRole.BOSS).brute().raidsVillages()
                .stats(300, 20, 14, 0.28).resist(0.8, 48).size(1.0f, 2.6f, 1.45f)
                .summons("mob_gnoll_raider", 4)
                .coins(150, 300)
                .drop(1.0f, rpg(RpgItem.SKULL_MACE))
                .drop(1.0f, stack(Items.DIAMOND, 3, 6))
                .drop(1.0f, stack(Items.IRON_BLOCK, 2, 4)));

        add(MobSpec.of("mob_storm_tyrant", "Storm Tyrant", MobRole.BOSS).fireproof().lightning()
                .stats(280, 18, 10, 0.30).resist(0.7, 48).size(0.9f, 2.6f, 1.4f)
                .summons("mob_storm_herald", 3)
                .coins(150, 300)
                .drop(1.0f, rpg(RpgItem.THUNDERCALLER))
                .drop(1.0f, stack(Items.GOLD_INGOT, 4, 8))
                .drop(0.8f, stack(Items.DIAMOND, 2, 4)));

        add(MobSpec.of("mob_lich_sovereign", "Lich Sovereign", MobRole.BOSS).brute()
                .stats(320, 17, 12, 0.26).resist(0.75, 48).size(0.9f, 2.7f, 1.45f)
                .aura(MobEffects.WEAKNESS, 1).onHit(MobEffects.WITHER, 80, 0)
                .summons("mob_bone_legionnaire", 5)
                .coins(150, 300)
                .drop(1.0f, rpg(RpgItem.NIGHTFALL_SCYTHE))
                .drop(0.5f, rpg(RpgItem.NECRO_CROWN))
                .drop(1.0f, stack(Items.BONE, 6, 10))
                .drop(0.8f, stack(Items.DIAMOND, 2, 4)));
    }
}
