package com.political.items;

import com.political.combat.StatManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Per-item RIGHT CLICK abilities for Skyblock-style gear. */
public final class ItemActiveAbilityEngine {

    private static final Random RNG = new Random();
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ItemActiveAbilityEngine() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemActiveAbility ability = resolveAbility(sp);
            if (ability == null) return InteractionResult.PASS;
            if (onCooldown(sp, ability)) {
                sp.sendSystemMessage(Component.literal(ability.displayName + " is on cooldown.")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (ability.manaCost > 0 && !StatManager.spendMana(sp, ability.manaCost)) {
                sp.sendSystemMessage(Component.literal("Not enough Mana.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (!cast(sp, (ServerLevel) world, ability)) return InteractionResult.PASS;
            setCooldown(sp, ability);
            EquipmentSlot wearSlot = abilitySlot(sp);
            ItemStack source = sp.getItemBySlot(wearSlot);
            if (!source.isEmpty() && !sp.isCreative()) {
                source.hurtAndBreak(1, sp, wearSlot);
            }
            return InteractionResult.SUCCESS;
        });
    }

    private static ItemActiveAbility resolveAbility(ServerPlayer sp) {
        String id = RpgItems.idOf(sp.getMainHandItem());
        if (id != null) {
            ItemActiveAbility a = ItemActiveAbility.forItem(id);
            if (a != null) return a;
        }
        for (EquipmentSlot slot : ARMOR) {
            id = RpgItems.idOf(sp.getItemBySlot(slot));
            if (id == null) continue;
            ItemActiveAbility a = ItemActiveAbility.forItem(id);
            if (a != null) return a;
        }
        return null;
    }

    private static EquipmentSlot abilitySlot(ServerPlayer sp) {
        String id = RpgItems.idOf(sp.getMainHandItem());
        if (id != null && ItemActiveAbility.forItem(id) != null) return EquipmentSlot.MAINHAND;
        for (EquipmentSlot slot : ARMOR) {
            id = RpgItems.idOf(sp.getItemBySlot(slot));
            if (id != null && ItemActiveAbility.forItem(id) != null) return slot;
        }
        return EquipmentSlot.MAINHAND;
    }

    private static boolean onCooldown(ServerPlayer p, ItemActiveAbility a) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + a.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static void setCooldown(ServerPlayer p, ItemActiveAbility a) {
        COOLDOWNS.put(p.getStringUUID() + "|" + a.name(),
                System.currentTimeMillis() + a.cooldownSeconds * 1000L);
    }

    private static boolean cast(ServerPlayer p, ServerLevel level, ItemActiveAbility a) {
        switch (a) {
            case SHADOWSTEP -> {
                LivingEntity t = lookTarget(p, 20);
                if (t == null) return false;
                Vec3 behind = t.position().subtract(t.getViewVector(1f).scale(1.5));
                p.teleportTo(behind.x, behind.y, behind.z);
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1, false, true));
                level.sendParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1, p.getZ(), 30, 0.3, 0.5, 0.3, 0.02);
            }
            case EMBER_STAFF -> {
                for (LivingEntity e : cone(p, 10, 0.45)) {
                    e.setRemainingFireTicks(140);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 10f);
                }
                particleCone(level, p, ParticleTypes.FLAME);
                level.sendParticles(ParticleTypes.LAVA, p.getX(), p.getY() + 1, p.getZ(), 12, 0.5, 0.5, 0.5, 0.01);
            }
            case FLAME_BURST -> {
                for (LivingEntity e : cone(p, 8, 0.5)) {
                    e.setRemainingFireTicks(100);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 8f);
                }
                particleCone(level, p, ParticleTypes.FLAME);
            }
            case VOID_RIFT -> {
                for (LivingEntity e : around(p, 6)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 12f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 80, 2, 1, 2, 0.1);
            }
            case STORM_CALL -> strike(level, aimPoint(p, 16));
            case MIDAS_TOUCH -> {
                p.addEffect(new MobEffectInstance(MobEffects.LUCK, 600, 2, false, true));
                com.political.politics.DataManager.addCoins(p.getStringUUID(), 50 + RNG.nextInt(50));
            }
            case EXECUTE_MARK -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                float dmg = t.getHealth() < t.getMaxHealth() * 0.25f ? 24f : 10f;
                t.hurtServer(level, level.damageSources().playerAttack(p), dmg);
            }
            case FROST_NOVA -> {
                for (LivingEntity e : around(p, 7)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
                    e.hurtServer(level, level.damageSources().playerAttack(p), 6f);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 1, 3, 0.05);
            }
            case VENOM_STRIKE -> {
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 2));
                t.hurtServer(level, level.damageSources().playerAttack(p), 7f);
            }
            case THUNDER_DASH -> {
                blink(p, 10);
                for (LivingEntity e : around(p, 4)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 9f);
                }
                strike(level, p.position());
            }
            case ABYSSAL_WAVE -> {
                for (LivingEntity e : cone(p, 10, 0.4)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 11f);
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
            case INSTANT_TUNNEL -> {
                net.minecraft.world.phys.Vec3 view = p.getViewVector(1f);
                BlockPos start = p.blockPosition();
                for (int i = 1; i <= 8; i++) {
                    BlockPos pos = start.offset((int) Math.round(view.x * i), 0, (int) Math.round(view.z * i));
                    for (int dy = -1; dy <= 2; dy++) {
                        level.setBlock(pos.offset(0, dy, 0), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
            case VEIN_SURGE -> p.addEffect(new MobEffectInstance(MobEffects.HASTE, 400, 4, false, true));
            case EXCAVATE -> p.addEffect(new MobEffectInstance(MobEffects.HASTE, 300, 3, false, true));
            case WORLD_CLEAVE -> {
                for (LivingEntity e : cone(p, 6, 0.6)) e.hurtServer(level, level.damageSources().playerAttack(p), 14f);
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case GUARDIAN_AEGIS -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true));
            }
            case WARDEN_FORTIFY -> p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 1, false, true));
            case VOID_SHROUD -> {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, false));
            }
            case INFERNO_CROWN -> {
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, true));
                for (Monster m : level.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(5))) {
                    m.setRemainingFireTicks(80);
                }
            }
            case STORM_SURGE -> {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 2, false, true));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 40, 1, 1, 1, 0.1);
            }
            case TIDE_BLESSING -> {
                p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0, false, true));
            }
            case SCHOLAR_FOCUS -> {
                StatManager.addMana(p, StatManager.getMaxMana(p) * 0.5);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, true));
            }
            case MERCHANTS_LUCK -> com.political.politics.DataManager.addCoins(p.getStringUUID(), 100);
            case GUARD_BLESSING -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 300, 1, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 1, false, true));
            }
            case MOON_SLASH -> {
                for (LivingEntity e : cone(p, 7, 0.55)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 11f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case RADIANT_NOVA -> {
                for (LivingEntity e : around(p, 8)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 9f);
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 50, 2, 1, 2, 0.05);
            }
            case SOUL_VOLLEY -> {
                for (LivingEntity e : around(p, 12)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 7f);
                }
                level.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1, p.getZ(), 40, 3, 1, 3, 0.04);
            }
            case DRAGON_ROAR -> {
                for (LivingEntity e : around(p, 6)) {
                    e.setRemainingFireTicks(100);
                    launchAway(e, p, 1.5);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 8f);
                }
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
            case ARACHNO_WEB -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                }
            }
            case NECROTIC_AURA -> {
                for (LivingEntity e : around(p, 7)) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    e.hurtServer(level, level.damageSources().playerAttack(p), 5f);
                }
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1, p.getZ(), 30, 2, 0.5, 2, 0.02);
            }
            case STARFALL -> {
                for (LivingEntity e : around(p, 5)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 14f);
                    launchAway(e, p, 0.8);
                }
                level.sendParticles(ParticleTypes.FIREWORK, p.getX(), p.getY(), p.getZ(), 20, 1, 0.5, 1, 0.1);
            }
            case LUNAR_MINE -> {
                p.addEffect(new MobEffectInstance(MobEffects.HASTE, 500, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 500, 0, false, false));
            }
            case ARCANE_HARVEST -> {
                StatManager.addMana(p, StatManager.getMaxMana(p) * 0.35);
                p.addEffect(new MobEffectInstance(MobEffects.HASTE, 300, 2, false, true));
            }
            case SOLAR_FLARE -> {
                LivingEntity t = lookTarget(p, 14);
                if (t == null) return false;
                t.setRemainingFireTicks(160);
                t.hurtServer(level, level.damageSources().playerAttack(p), 13f);
                strike(level, t.position());
            }
            case BERSERK_RAGE -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 240, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 240, 1, false, true));
            }
            case SHIELD_BASH -> {
                LivingEntity t = lookTarget(p, 5);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 8f);
                launchAway(t, p, 1.4);
            }
            case TIDAL_LANCE -> {
                LivingEntity t = lookTarget(p, 10);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 12f);
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                level.sendParticles(ParticleTypes.SPLASH, t.getX(), t.getY() + 1, t.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
            }
            case BLOOD_DRAIN -> {
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                float dmg = 9f;
                t.hurtServer(level, level.damageSources().playerAttack(p), dmg);
                p.heal(dmg * 0.5f);
            }
            case SKULL_CRUSH -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 20f);
                level.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.1);
            }
            case CRYSTAL_BEAM -> {
                LivingEntity t = lookTarget(p, 16);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 11f);
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                strike(level, t.position());
            }
            case GHOST_STRIKE -> {
                LivingEntity t = lookTarget(p, 12);
                if (t == null) return false;
                blink(p, 6);
                t.hurtServer(level, level.damageSources().playerAttack(p), 10f);
                level.sendParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1, p.getZ(), 20, 0.2, 0.4, 0.2, 0.02);
            }
            case TIMBER_CHOP -> p.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 4, false, true));
            case PROSPECTOR_DIG -> {
                p.addEffect(new MobEffectInstance(MobEffects.HASTE, 300, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.LUCK, 300, 1, false, true));
            }
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1.2f);
        return true;
    }

    // --- helpers (mirrors PowerManager patterns) ---

    private static LivingEntity lookTarget(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        LivingEntity best = null;
        double bestAlong = range;
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(2);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (to.subtract(view.scale(along)).lengthSqr() <= 2.5 && along < bestAlong) {
                best = e;
                bestAlong = along;
            }
        }
        return best;
    }

    private static List<LivingEntity> cone(ServerPlayer p, double range, double tightness) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        java.util.ArrayList<LivingEntity> out = new java.util.ArrayList<>();
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(range * 0.5 + 1);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (along / to.length() >= tightness) out.add(e);
        }
        return out;
    }

    private static List<LivingEntity> around(ServerPlayer p, double r) {
        return p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), x -> x != p && x.isAlive());
    }

    private static Vec3 aimPoint(ServerPlayer p, double d) {
        return p.getEyePosition().add(p.getViewVector(1f).scale(d));
    }

    private static void blink(ServerPlayer p, double dist) {
        Vec3 v = p.getViewVector(1f).scale(dist);
        p.teleportTo(p.getX() + v.x, p.getY() + Math.max(0, v.y), p.getZ() + v.z);
        p.fallDistance = 0;
    }

    private static void launchAway(LivingEntity e, ServerPlayer p, double s) {
        Vec3 d = e.position().subtract(p.position()).normalize().scale(s);
        e.push(d.x, 0.3, d.z);
        e.hurtMarked = true;
    }

    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

    private static void particleCone(ServerLevel level, ServerPlayer p, net.minecraft.core.particles.SimpleParticleType pt) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(pt, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
