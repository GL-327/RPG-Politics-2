package com.political.expansion2.mobs;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class ExpansionMob2 extends Monster {

    private final MobSpec2 spec;
    private ServerBossEvent bossBar;
    private int phase = 1;
    private int abilityCooldown = 0;

    public ExpansionMob2(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.spec = ExpansionMobs2.specForType(type);
        if (spec != null && spec.role.isBossLike()) {
            this.bossBar = new ServerBossEvent(
                    getUUID(),
                    Component.literal(spec.name),
                    spec.role == MobRole2.BOSS ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE,
                    BossEvent.BossBarOverlay.PROGRESS);
            setPersistenceRequired();
            setCustomName(Component.literal(spec.name).withStyle(ChatFormatting.RED));
            setCustomNameVisible(true);
        }
    }

    public MobSpec2 spec() {
        return spec;
    }

    public static AttributeSupplier.Builder createAttributes(MobSpec2 spec) {
        com.political.config.PoliticalConfig cfg = com.political.config.PoliticalConfig.get();
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, cfg.scaleMobHealth(spec.maxHealth))
                .add(Attributes.ATTACK_DAMAGE, cfg.scaleMobDamage(spec.attackDamage))
                .add(Attributes.ARMOR, spec.armor)
                .add(Attributes.MOVEMENT_SPEED, spec.speed)
                .add(Attributes.FOLLOW_RANGE, spec.followRange)
                .add(Attributes.KNOCKBACK_RESISTANCE, spec.knockbackResist)
                .add(Attributes.SCALE, spec.scale);
    }

    @Override
    protected void registerGoals() {
        if (spec == null) return;

        double moveSpeed = spec.role.isBossLike() ? 1.0 : 0.95;
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, moveSpeed, true));
        if (spec.role == MobRole2.SKITTISH) {
            goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.1, 1.4));
        }
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (spec.role.isAggressive()) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            if (spec.attacksVillagers) {
                targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() || spec == null) return;

        if (abilityCooldown > 0) abilityCooldown--;

        if (bossBar != null) {
            float max = getMaxHealth();
            bossBar.setProgress(max > 0 ? getHealth() / max : 0f);
            refreshBossPlayers();
            if (spec.role == MobRole2.BOSS && phase == 1 && getHealth() < max * 0.5f) {
                enterPhaseTwo();
            }
        }

        LivingEntity target = getTarget();
        boolean inMelee = target != null && distanceToSqr(target) <= 9.0;

        if (inMelee && tickCount % 12 == 0) {
            if (spec.ignites) target.setRemainingFireTicks(80);
            if (spec.onHitEffect != null) {
                target.addEffect(new MobEffectInstance(
                        spec.onHitEffect, spec.onHitDuration, spec.onHitAmplifier, false, true));
            }
            if (spec.lifesteal > 0 && getHealth() < getMaxHealth()) {
                heal(spec.lifesteal);
            }
        }

        if (spec.auraEffect != null && tickCount % 40 == 0) {
            for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(6.0))) {
                if (p.isCreative() || p.isSpectator()) continue;
                p.addEffect(new MobEffectInstance(spec.auraEffect, 120, spec.auraAmplifier, false, true));
            }
        }

        if (spec.callsLightning && target != null && abilityCooldown == 0
                && distanceToSqr(target) <= 36.0 * 36.0) {
            strikeLightning(target);
            abilityCooldown = spec.role == MobRole2.BOSS ? 60 : 140;
        }
    }

    private void enterPhaseTwo() {
        phase = 2;
        addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, false));
        addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        if (level() instanceof ServerLevel server) {
            if (spec.summonAddId != null) {
                for (int i = 0; i < spec.summonAddCount; i++) {
                    ExpansionMobs2.summonAdd(server, this, spec.summonAddId);
                }
            }
            for (ServerPlayer p : server.getPlayers(p -> p.distanceToSqr(this) < 48.0 * 48.0)) {
                p.sendSystemMessage(Component.literal(spec.name + " enters a furious second phase!")
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    private void strikeLightning(LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) return;
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(server, EntitySpawnReason.TRIGGERED);
        if (bolt == null) return;
        bolt.setPos(target.getX(), target.getY(), target.getZ());
        server.addFreshEntity(bolt);
    }

    private void refreshBossPlayers() {
        if (!(level() instanceof ServerLevel server)) return;
        List<ServerPlayer> near = server.getPlayers(p -> p.distanceToSqr(this) < 48.0 * 48.0);
        for (ServerPlayer existing : List.copyOf(bossBar.getPlayers())) {
            if (!near.contains(existing)) bossBar.removePlayer(existing);
        }
        for (ServerPlayer p : near) bossBar.addPlayer(p);
    }

    public int getPhase() {
        return phase;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (bossBar != null) bossBar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("Exp2Phase", phase);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.phase = Math.max(1, in.getIntOr("Exp2Phase", 1));
    }

    public static boolean checkSpawnRules(EntityType<ExpansionMob2> type,
                                          net.minecraft.world.level.ServerLevelAccessor level,
                                          EntitySpawnReason reason,
                                          BlockPos pos,
                                          net.minecraft.util.RandomSource random) {
        return ExpansionMobs2.naturalSpawnsEnabled
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
