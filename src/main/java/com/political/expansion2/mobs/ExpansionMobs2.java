package com.political.expansion2.mobs;

import com.political.politics.DataManager;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class ExpansionMobs2 {

    public static final String MOD_ID = "politicalserver";

    public static final List<MobSpec2> SPECS = new ArrayList<>();

    private static final Map<String, MobSpec2> BY_ID = new HashMap<>();
    private static final Map<EntityType<?>, MobSpec2> BY_TYPE = new HashMap<>();
    private static final Random RNG = new Random();

    public static boolean naturalSpawnsEnabled = true;

    private static boolean bootstrapped = false;

    private ExpansionMobs2() {}

    public static void register() {
        bootstrap();
        for (MobSpec2 spec : SPECS) registerType(spec);
        registerSpawns();
        registerDeathRewards();
    }

    public static MobSpec2 specForType(EntityType<?> type) {
        return BY_TYPE.get(type);
    }

    public static MobSpec2 specById(String id) {
        return BY_ID.get(id);
    }

    public static List<String> ids() {
        List<String> out = new ArrayList<>(SPECS.size());
        for (MobSpec2 s : SPECS) out.add(s.id);
        return out;
    }

    public static List<EntityType<ExpansionMob2>> allTypes() {
        List<EntityType<ExpansionMob2>> out = new ArrayList<>(SPECS.size());
        for (MobSpec2 s : SPECS) {
            if (s.type != null) out.add(s.type);
        }
        return out;
    }

    public static ExpansionMob2 spawnById(ServerLevel level, BlockPos pos, String id) {
        MobSpec2 spec = BY_ID.get(id);
        if (spec == null || spec.type == null) return null;
        ExpansionMob2 mob = spec.type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return null;
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(mob);
        return mob;
    }

    static void summonAdd(ServerLevel level, ExpansionMob2 boss, String id) {
        MobSpec2 spec = BY_ID.get(id);
        if (spec == null || spec.type == null) return;
        ExpansionMob2 mob = spec.type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos(boss.getX() + (RNG.nextDouble() - 0.5) * 4.0,
                boss.getY(),
                boss.getZ() + (RNG.nextDouble() - 0.5) * 4.0);
        level.addFreshEntity(mob);
    }

    static MobSpec2 add(MobSpec2 spec) {
        SPECS.add(spec);
        BY_ID.put(spec.id, spec);
        return spec;
    }

    private static void registerType(MobSpec2 spec) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, spec.id));
        EntityType.Builder<ExpansionMob2> builder = EntityType.Builder
                .<ExpansionMob2>of(ExpansionMob2::new, spec.category)
                .sized(spec.width, spec.height);
        if (spec.fireImmune) builder = builder.fireImmune();
        EntityType<ExpansionMob2> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
        FabricDefaultAttributeRegistry.register(type, ExpansionMob2.createAttributes(spec));
        spec.type = type;
        BY_TYPE.put(type, spec);
    }

    private static void registerSpawns() {
        for (MobSpec2 spec : SPECS) {
            if (spec.type == null) continue;
            SpawnPlacements.register(spec.type, SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ExpansionMob2::checkSpawnRules);
            if (spec.spawnWeight > 0) {
                int weight = Math.max(1, (spec.spawnWeight * 2 + 2) / 3);
                weight = com.political.config.PoliticalConfig.get().scaleSpawnWeight(weight);
                BiomeModifications.addSpawn(spec.biomeSelector, MobCategory.MONSTER, spec.type,
                        weight, spec.minGroup, spec.maxGroup);
            }
        }
    }

    private static void registerDeathRewards() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ExpansionMob2 mob)) return;
            MobSpec2 spec = mob.spec();
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

            for (MobSpec2.Drop drop : spec.drops) {
                if (RNG.nextFloat() >= drop.chance) continue;
                var stack = drop.factory.get();
                if (stack == null || stack.isEmpty()) continue;
                level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                        level, mob.getX(), mob.getY() + 0.5, mob.getZ(), stack));
            }
        });
    }

    private static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        MobRoster2.bootstrap();
    }
}
