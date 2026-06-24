package com.political.combat;

import com.political.items.Ability;
import com.political.items.ItemStats;
import com.political.items.RpgItems;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Event-driven gear abilities (no mixins). Hooks attack, block-break, and a periodic
 * equipment tick. Abilities are read from each stack's {@code custom_data} via
 * {@link RpgItems#abilitiesOf}.
 */
public final class AbilityEngine {

    private static final Random RNG = new Random();
    private static final ThreadLocal<Boolean> BREAKING = ThreadLocal.withInitial(() -> false);
    private static final int MAX_VEIN = 64;

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private AbilityEngine() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return InteractionResult.PASS;
            // When the Skyblock damage system is disabled, defer to vanilla combat entirely.
            if (!com.political.config.PoliticalConfig.get().combatDamageEnabled) return InteractionResult.PASS;
            onAttack(sp, target);
            return InteractionResult.SUCCESS; // Skyblock damage replaces vanilla hit
        });

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level.isClientSide() || BREAKING.get()) return;
            if (!(player instanceof ServerPlayer sp)) return;
            if (!(level instanceof ServerLevel serverLevel)) return;
            onBlockBreak(sp, serverLevel, pos, state);
        });
    }

    // ---------------- On attack ----------------

    private static void onAttack(ServerPlayer attacker, LivingEntity target) {
        ServerLevel level = (ServerLevel) target.level();
        com.political.config.PoliticalConfig cfg = com.political.config.PoliticalConfig.get();
        ItemStack weapon = attacker.getMainHandItem();
        ItemStats.Sheet sheet = ItemStats.compute(weapon);
        RpgStats stats = StatManager.get(attacker);

        // Hypixel-Skyblock melee model:
        //   additive   = base + weaponDamage + floor(strength/5) * strengthDamageScale
        //   strengthMul= 1 + strength/100 * strengthMultiplierPct
        //   base       = additive * strengthMul * damageMultiplier * cursedOutput
        // Strength therefore contributes both flat (via the floor term) and multiplicatively,
        // exactly like real Skyblock. Cursed-energy output/reinforcement (flow) scales it too.
        double additive = cfg.damageBaseFlat + sheet.damage
                + Math.floor(stats.strength / 5.0) * cfg.strengthDamageScale;
        double strengthMul = 1.0 + (stats.strength / 100.0) * cfg.strengthMultiplierPct;
        double base = additive * strengthMul * cfg.damageMultiplier
                * com.political.curse.rules.JjkRules.outputMultiplier(attacker);

        float cursedBonus = com.political.curse.CursedGear.attackBonus(attacker, weapon);
        // Cursed-prefixed gear adds a flat cursed-energy bite on top, scaled by the wielder's reserves.
        cursedBonus += com.political.items.PrefixAbilities.onHitBonus(attacker, weapon);
        if (cursedBonus > 0) {
            base += cursedBonus;
            com.political.curse.CursedGear.playHitFx(level, target);
        }

        applyCritAndFerocity(attacker, target, level, base);

        // Black Flash (cursed technique buff) must be consumed here: this engine returns SUCCESS
        // from its AttackEntityCallback, which would short-circuit any other attack listener.
        com.political.power.PowerManager.tryConsumeBlackFlash(attacker, target, level);
        // Cursed-prefixed weapons have an innate chance to land a Black Flash of their own.
        com.political.items.PrefixAbilities.tryWeaponBlackFlash(attacker, weapon, target, level);

        List<Ability> abilities = RpgItems.abilitiesOf(attacker.getMainHandItem());
        if (abilities.isEmpty()) return;

        for (Ability a : abilities) {
            switch (a) {
                case LIFESTEAL -> attacker.heal(2.0f);
                case IGNITE -> target.setRemainingFireTicks(100);
                case POISON -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                case FROST -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                case WITHER_TOUCH -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                case KNOCKBACK -> {
                    double dx = target.getX() - attacker.getX();
                    double dz = target.getZ() - attacker.getZ();
                    double len = Math.sqrt(dx * dx + dz * dz);
                    if (len > 1.0e-4) {
                        target.push(dx / len * 0.9, 0.4, dz / len * 0.9);
                        target.hurtMarked = true;
                    }
                }
                case THUNDER_STRIKE -> {
                    if (RNG.nextFloat() < 0.25f) strike(level, target);
                }
                case CRIT_STRIKE -> {
                    if (RNG.nextFloat() < 0.30f) {
                        target.hurtServer(level, level.damageSources().playerAttack(attacker), 6.0f);
                    }
                }
                case EXECUTE -> {
                    if (target.getHealth() < target.getMaxHealth() * 0.20f) {
                        target.hurtServer(level, level.damageSources().playerAttack(attacker), 10.0f);
                    }
                }
                default -> { }
            }
        }
    }

    /**
     * Applies the Skyblock hit: rolls crit (a single damage event multiplied by
     * {@code 1 + critDamage/100}), then ferocity extra strikes that each deal a full hit.
     * {@code base} is the pre-crit damage from {@link #onAttack}.
     */
    private static void applyCritAndFerocity(ServerPlayer attacker, LivingEntity target, ServerLevel level, double base) {
        RpgStats s = StatManager.get(attacker);
        com.political.config.PoliticalConfig cfg = com.political.config.PoliticalConfig.get();

        boolean crit = false;
        if (cfg.critEnabled) {
            double critChance = Math.min(cfg.maxCritChancePercent, s.critChance * cfg.critChanceMultiplier);
            crit = critChance > 0 && RNG.nextDouble() * 100.0 < critChance;
        }
        double hit = crit
                ? base * (1.0 + (s.critDamage * cfg.critDamageMultiplier) / 100.0)
                : base;
        hit = Math.min(hit, cfg.maxHitDamage);

        target.invulnerableTime = 0;
        target.hurtServer(level, level.damageSources().playerAttack(attacker), (float) hit);
        if (crit) {
            level.sendParticles(ParticleTypes.CRIT, target.getX(),
                    target.getY() + target.getBbHeight() * 0.6, target.getZ(), 12, 0.3, 0.3, 0.3, 0.1);
        }

        if (!cfg.ferocityEnabled || s.ferocity <= 0) return;
        // Ferocity = guaranteed extra strikes for every 100%, plus a chance for one more from the
        // remainder. Each strike repeats the full (crit-rolled) hit, as in Skyblock.
        int extra = (int) (s.ferocity / 100.0);
        if (RNG.nextDouble() * 100.0 < s.ferocity % 100.0) extra++;
        float ferDmg = (float) Math.min(cfg.maxHitDamage, hit * cfg.ferocityStrikeMultiplier);
        for (int i = 0; i < extra; i++) {
            target.invulnerableTime = 0;
            target.hurtServer(level, level.damageSources().playerAttack(attacker), ferDmg);
        }
    }

    private static void strike(ServerLevel level, Entity at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.getX(), at.getY(), at.getZ());
            level.addFreshEntity(bolt);
        }
    }

    // ---------------- On block break ----------------

    private static void onBlockBreak(ServerPlayer player, ServerLevel level, BlockPos origin, BlockState state) {
        List<Ability> abilities = RpgItems.abilitiesOf(player.getMainHandItem());
        if (abilities.isEmpty()) return;
        EnumSet<Ability> set = EnumSet.copyOf(abilities);
        boolean autoSmelt = set.contains(Ability.AUTO_SMELT);
        boolean fortune = set.contains(Ability.FORTUNE_TOUCH);

        Set<BlockPos> targets = new HashSet<>();
        if (set.contains(Ability.TREE_FELLER) && isLog(state)) {
            collectConnected(level, origin, targets, AbilityEngine::isLog);
        }
        if (set.contains(Ability.VEIN_MINE) && isOre(state)) {
            collectConnected(level, origin, targets, s -> s.is(state.getBlock()));
        }
        if (set.contains(Ability.TUNNEL_3X3)) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos p = origin.offset(dx, dy, dz);
                        if (!p.equals(origin) && isBreakable(level.getBlockState(p))) targets.add(p);
                    }
        }
        if (targets.isEmpty()) return;

        BREAKING.set(true);
        try {
            for (BlockPos p : targets) {
                BlockState s = level.getBlockState(p);
                if (!isBreakable(s)) continue;
                level.removeBlock(p, false);
                dropFor(level, p, s, autoSmelt, fortune);
            }
        } finally {
            BREAKING.set(false);
        }
    }

    private static void collectConnected(ServerLevel level, BlockPos start, Set<BlockPos> out,
                                         java.util.function.Predicate<BlockState> match) {
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        Set<BlockPos> seen = new HashSet<>();
        seen.add(start);
        while (!queue.isEmpty() && out.size() < MAX_VEIN) {
            BlockPos cur = queue.poll();
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = cur.offset(dx, dy, dz);
                        if (seen.contains(n)) continue;
                        seen.add(n);
                        if (match.test(level.getBlockState(n))) {
                            out.add(n);
                            queue.add(n);
                        }
                    }
        }
    }

    private static void dropFor(ServerLevel level, BlockPos pos, BlockState state, boolean autoSmelt, boolean fortune) {
        ItemStack drop = smelted(state, autoSmelt);
        if (drop.isEmpty()) {
            drop = new ItemStack(state.getBlock());
        }
        if (fortune && isOre(state)) {
            drop.grow(1 + RNG.nextInt(2));
        }
        Block.popResource(level, pos, drop);
    }

    private static ItemStack smelted(BlockState state, boolean autoSmelt) {
        Block b = state.getBlock();
        if (autoSmelt) {
            if (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE) return new ItemStack(Items.IRON_INGOT);
            if (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE) return new ItemStack(Items.GOLD_INGOT);
            if (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE) return new ItemStack(Items.COPPER_INGOT);
            if (b == Blocks.ANCIENT_DEBRIS) return new ItemStack(Items.NETHERITE_SCRAP);
        }
        if (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE) return new ItemStack(Items.DIAMOND);
        if (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE) return new ItemStack(Items.EMERALD);
        if (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE) return new ItemStack(Items.COAL);
        if (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE) return new ItemStack(Items.LAPIS_LAZULI);
        if (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE) return new ItemStack(Items.REDSTONE);
        if (!autoSmelt && (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE)) return new ItemStack(Items.RAW_IRON);
        if (!autoSmelt && (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE)) return new ItemStack(Items.RAW_GOLD);
        if (!autoSmelt && (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE)) return new ItemStack(Items.RAW_COPPER);
        return ItemStack.EMPTY;
    }

    private static boolean isLog(BlockState s) {
        return s.is(net.minecraft.tags.BlockTags.LOGS);
    }

    private static boolean isOre(BlockState s) {
        Block b = s.getBlock();
        return b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE
                || b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE
                || b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE
                || b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE
                || b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE
                || b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE
                || b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE
                || b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE
                || b == Blocks.NETHER_GOLD_ORE || b == Blocks.NETHER_QUARTZ_ORE
                || b == Blocks.ANCIENT_DEBRIS;
    }

    private static boolean isBreakable(BlockState s) {
        return !s.isAir() && s.getBlock() != Blocks.BEDROCK && !s.liquid();
    }

    // ---------------- Equipment tick ----------------

    /** Called periodically (about once per second) from the server tick loop. */
    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            EnumSet<Ability> active = EnumSet.noneOf(Ability.class);
            for (EquipmentSlot slot : ARMOR) {
                active.addAll(RpgItems.abilitiesOf(player.getItemBySlot(slot)));
            }
            active.addAll(RpgItems.abilitiesOf(player.getMainHandItem()));
            active.addAll(RpgItems.abilitiesOf(player.getItemBySlot(EquipmentSlot.OFFHAND)));
            applyPassives(player, active);
        }
    }

    private static void applyPassives(ServerPlayer player, EnumSet<Ability> active) {
        int dur = 60; // ticks; refreshed each pass
        if (active.contains(Ability.NIGHT_VISION))
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
        if (active.contains(Ability.WATER_BREATHING))
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur + 20, 0, true, false));
        if (active.contains(Ability.SPEED))
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, dur + 20, 0, true, false));
        if (active.contains(Ability.HASTE) || active.contains(Ability.INSTANT_MINE))
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, dur + 20, active.contains(Ability.INSTANT_MINE) ? 4 : 1, true, false));
        if (active.contains(Ability.REGEN))
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur + 20, 0, true, false));
        if (active.contains(Ability.ABSORPTION))
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, dur + 20, 1, true, false));
        if (active.contains(Ability.RESISTANCE))
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, dur + 20, 0, true, false));
        if (active.contains(Ability.FIRE_IMMUNE))
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur + 20, 0, true, false));
        if (active.contains(Ability.FALL_IMMUNE))
            player.fallDistance = 0;
        if (active.contains(Ability.FIRE_AURA)) {
            AABB box = player.getBoundingBox().inflate(4.0);
            for (Monster m : player.level().getEntitiesOfClass(Monster.class, box)) {
                m.setRemainingFireTicks(80);
            }
        }
        manageFlight(player, active.contains(Ability.FLIGHT));
    }

    private static void manageFlight(ServerPlayer player, boolean shouldFly) {
        // Route gear-granted flight through the unified Viltrumite FlightManager rather than
        // toggling raw mayfly here. Creative/spectator flight is left to vanilla by FlightManager.
        com.political.flight.FlightManager.setArmorFlight(player, shouldFly);
    }

    /** Helper for other systems (e.g. economy boosts): is an ability worn/held anywhere? */
    public static boolean hasEquipped(ServerPlayer player, Ability ability) {
        for (EquipmentSlot slot : ARMOR) {
            if (RpgItems.abilitiesOf(player.getItemBySlot(slot)).contains(ability)) return true;
        }
        return RpgItems.abilitiesOf(player.getMainHandItem()).contains(ability);
    }

    public static void onPlayerRemoved(UUID uuid) {
        com.political.flight.FlightManager.onPlayerRemoved(uuid);
    }
}
