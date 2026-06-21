package com.political.expansion2.melee;

import com.political.combat.StatManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Self-contained RIGHT CLICK handler for {@code wpn2_*} weapons. */
public final class Melee2AbilityEngine {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private Melee2AbilityEngine() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            Melee2Weapon weapon = Melee2Weapons.byStack(sp.getMainHandItem());
            if (weapon == null) return InteractionResult.PASS;
            Melee2Ability ability = weapon.ability;

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
            return InteractionResult.SUCCESS;
        });
    }

    private static boolean onCooldown(ServerPlayer p, Melee2Ability a) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + a.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static void setCooldown(ServerPlayer p, Melee2Ability a) {
        COOLDOWNS.put(p.getStringUUID() + "|" + a.name(),
                System.currentTimeMillis() + a.cooldownSeconds * 1000L);
    }

    private static boolean cast(ServerPlayer p, ServerLevel level, Melee2Ability a) {
        switch (a) {
            case CURSED_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 8f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
            case CURSED_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 8.8f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case CURSED_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 8.5f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                }
                level.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case CURSED_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 9.5f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
            case CURSED_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 10.3f);
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case CURSED_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 10f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
            case CURSED_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 12f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
            case CURSED_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 11f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.REVERSE_PORTAL);
            }
            case CURSED_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 13.8f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case CURSED_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 12.6f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                }
                level.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case CURSED_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 15.6f);
                    level.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case CURSED_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 12f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
            case PROM_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 12f);
                }
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case PROM_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 14.3f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.sendParticles(ParticleTypes.FIREWORK, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case PROM_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 12.3f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                }
                level.sendParticles(ParticleTypes.GLOW, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case PROM_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 13.7f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case PROM_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 16.1f);
                particleCone(level, p, ParticleTypes.FIREWORK);
            }
            case PROM_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 14f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case PROM_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 16.8f);
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case PROM_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 16.5f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.FIREWORK);
            }
            case PROM_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 18.8f);
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.GLOW, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case PROM_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 17.3f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                }
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case PROM_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 20.8f);
                    level.sendParticles(ParticleTypes.FIREWORK, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case PROM_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 16f);
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case ELEM_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 16f);
                    e.setRemainingFireTicks(120);
                }
                particleCone(level, p, ParticleTypes.FLAME);
            }
            case ELEM_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 18.7f);
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
                level.sendParticles(ParticleTypes.SNOWFLAKE, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case ELEM_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 16.1f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 2));
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case ELEM_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 17.9f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                    e.setRemainingFireTicks(120);
                }
                particleCone(level, p, ParticleTypes.BUBBLE);
            }
            case ELEM_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 20.7f);
                particleCone(level, p, ParticleTypes.EXPLOSION);
            }
            case ELEM_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 18f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                }
                particleCone(level, p, ParticleTypes.LAVA);
            }
            case ELEM_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 22.8f);
                    e.setRemainingFireTicks(120);
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.FLAME);
            }
            case ELEM_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 20.9f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
                }
                particleCone(level, p, ParticleTypes.SNOWFLAKE);
            }
            case ELEM_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 23.8f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case ELEM_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 23f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                    e.setRemainingFireTicks(120);
                }
                level.sendParticles(ParticleTypes.BUBBLE, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case ELEM_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 26f);
                    level.sendParticles(ParticleTypes.EXPLOSION, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case ELEM_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 20f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.LAVA);
            }
            case VOID_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 21f);
                }
                particleCone(level, p, ParticleTypes.REVERSE_PORTAL);
            }
            case VOID_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 23.1f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.sendParticles(ParticleTypes.PORTAL, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case VOID_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 19.9f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 2));
                }
                level.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case VOID_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 23.1f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                particleCone(level, p, ParticleTypes.WITCH);
            }
            case VOID_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 25.3f);
                particleCone(level, p, ParticleTypes.REVERSE_PORTAL);
            }
            case VOID_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 22f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case VOID_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 27.6f);
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.REVERSE_PORTAL);
            }
            case VOID_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 25.3f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.PORTAL);
            }
            case VOID_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 28.8f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case VOID_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 27.6f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 3));
                }
                level.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case VOID_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 31.2f);
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case VOID_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 24f);
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.END_ROD);
            }
            case CEL_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 25f);
                }
                particleCone(level, p, ParticleTypes.END_ROD);
                p.heal(0.15f * cone(p, 6, 0.5).size());
            }
            case CEL_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 27.5f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.sendParticles(ParticleTypes.FIREWORK, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case CEL_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 24.7f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                }
                level.sendParticles(ParticleTypes.GLOW, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case CEL_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 27.3f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                particleCone(level, p, ParticleTypes.END_ROD);
                p.heal(0.15f * cone(p, 7, 0.45).size());
            }
            case CEL_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 29.9f);
                particleCone(level, p, ParticleTypes.FIREWORK);
            }
            case CEL_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 27f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.END_ROD);
                p.heal(0.15f * cone(p, 10, 0.55).size());
            }
            case CEL_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 32.4f);
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.END_ROD);
                p.heal(0.15f * cone(p, 8, 0.4).size());
            }
            case CEL_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 29.7f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.FIREWORK);
                p.heal(0.15f * cone(p, 7, 0.45).size());
            }
            case CEL_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 35f);
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.GLOW, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case CEL_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 32.2f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                }
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case CEL_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 36.4f);
                    level.sendParticles(ParticleTypes.FIREWORK, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case CEL_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 29f);
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.END_ROD);
                p.heal(0.15f * cone(p, 12, 0.6).size());
            }
            case BLOOD_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 29f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case BLOOD_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 31.9f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case BLOOD_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 28.5f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                }
                level.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case BLOOD_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 31.5f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
            case BLOOD_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 34.5f);
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case BLOOD_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 31f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.DAMAGE_INDICATOR);
            }
            case BLOOD_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 37.2f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case BLOOD_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 34.1f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.DAMAGE_INDICATOR);
            }
            case BLOOD_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 40f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case BLOOD_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 36.8f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                }
                level.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case BLOOD_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 42.9f);
                    level.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case BLOOD_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 33f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.DAMAGE_INDICATOR);
            }
            case TECH_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 33f);
                }
                particleCone(level, p, ParticleTypes.ELECTRIC_SPARK);
            }
            case TECH_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 37.4f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case TECH_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 32.3f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case TECH_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 35.7f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                particleCone(level, p, ParticleTypes.FIREWORK);
            }
            case TECH_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 40.3f);
                particleCone(level, p, ParticleTypes.ELECTRIC_SPARK);
            }
            case TECH_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 35f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case TECH_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 42f);
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.ELECTRIC_SPARK);
            }
            case TECH_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 39.6f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case TECH_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 45f);
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case TECH_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 41.4f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                }
                level.sendParticles(ParticleTypes.FIREWORK, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
            }
            case TECH_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 48.1f);
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case TECH_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 37f);
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case NAT_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 37f);
                }
                particleCone(level, p, ParticleTypes.HAPPY_VILLAGER);
            }
            case NAT_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 41.8f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
            }
            case NAT_CRUSH -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 36.1f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                }
                level.sendParticles(ParticleTypes.COMPOSTER, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
                float healed = around(p, 5).size() * 0.2f;
                if (healed > 0) p.heal(Math.min(healed, 15f));
            }
            case NAT_REND -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 39.9f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                particleCone(level, p, ParticleTypes.HAPPY_VILLAGER);
            }
            case NAT_LUNGE -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                hurt(level, p, t, 44.8f);
                particleCone(level, p, ParticleTypes.SPORE_BLOSSOM_AIR);
            }
            case NAT_REACH -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    hurt(level, p, e, 39f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.COMPOSTER);
            }
            case NAT_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.4)) {
                    hurt(level, p, e, 48f);
                    launchAway(e, p, 1.2);
                }
                particleCone(level, p, ParticleTypes.HAPPY_VILLAGER);
            }
            case NAT_REAP -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 44f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.SPORE_BLOSSOM_AIR);
            }
            case NAT_SMASH -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 50f);
                    launchAway(e, p, 1.3);
                }
                level.sendParticles(ParticleTypes.COMPOSTER, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
                float healed = around(p, 7).size() * 0.2f;
                if (healed > 0) p.heal(Math.min(healed, 15f));
            }
            case NAT_CHOP -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 47.1f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                }
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
                float healed = around(p, 6).size() * 0.2f;
                if (healed > 0) p.heal(Math.min(healed, 15f));
            }
            case NAT_FRENZY -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 53.3f);
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }
            case NAT_LASH -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 41f);
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.1);
                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.COMPOSTER);
            }
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1.2f);
        return true;
    }

    private static void hurt(ServerLevel level, ServerPlayer p, LivingEntity e, float dmg) {
        e.hurtServer(level, level.damageSources().playerAttack(p), dmg);
    }

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
        List<LivingEntity> out = new ArrayList<>();
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

    private static void particleCone(ServerLevel level, ServerPlayer p, SimpleParticleType pt) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(pt, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
