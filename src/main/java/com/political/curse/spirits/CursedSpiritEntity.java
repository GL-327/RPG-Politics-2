package com.political.curse.spirits;

import com.political.curse.CurseEntity;
import com.political.curse.CurseManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shared entity for every {@link SpiritSpecies}. One {@code EntityType} is registered per species
 * (see {@link ModSpirits}); the entity resolves its species from its own type, then drives all
 * special mechanics mixin-free inside {@link #tick()} with per-trait cooldowns, on top of the
 * inherited vanilla zombie goals (path / melee / target acquisition). Boss species also run a
 * self-managed {@link ServerBossEvent} health bar.
 */
public class CursedSpiritEntity extends CurseEntity {

    private SpiritSpecies species;
    private ServerBossEvent bossBar;
    private boolean enraged;

    private int blastCd;
    private int auraCd;
    private int teleportCd;
    private int summonCd;
    private int shockCd;
    private int meleeCd;

    public CursedSpiritEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    /** Resolves (and caches) the species this entity's type was registered with. */
    public SpiritSpecies species() {
        if (species == null) species = ModSpirits.speciesFor(getType());
        return species;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel sl)) return;
        SpiritSpecies sp = species();
        if (sp == null) return;

        if (sp.has(Behavior.FIRE_IMMUNE)) setRemainingFireTicks(0);
        if (sp.has(Behavior.REGEN) && tickCount % 40 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0f + getGrade() * 0.5f);
        }
        if (sp.has(Behavior.ENRAGE) && !enraged && getHealth() < getMaxHealth() * 0.30f) {
            enrage(sl);
        }
        if (sp.boss()) updateBossBar(sl);

        LivingEntity target = getTarget();
        if (blastCd > 0) blastCd--;
        if (auraCd > 0) auraCd--;
        if (teleportCd > 0) teleportCd--;
        if (summonCd > 0) summonCd--;
        if (shockCd > 0) shockCd--;
        if (meleeCd > 0) meleeCd--;

        if ((sp.has(Behavior.POISON_AURA) || sp.has(Behavior.FROST_AURA) || sp.has(Behavior.WITHER_TOUCH))
                && auraCd <= 0) {
            if (applyAuras(sl, sp)) auraCd = 40;
        }
        if (target != null && (sp.has(Behavior.RANGED_BLAST) || sp.has(Behavior.FIRE_BLAST))
                && blastCd <= 0 && distanceTo(target) <= 22.0 && getSensing().hasLineOfSight(target)) {
            fireBlast(sl, sp, target);
            blastCd = sp.boss() ? 30 : 50;
        }
        if (target != null && sp.has(Behavior.TELEPORT) && teleportCd <= 0
                && (distanceTo(target) > 8.0 || getRandom().nextFloat() < 0.05f)) {
            if (blink(sl, target)) teleportCd = sp.boss() ? 60 : 110;
        }
        if (target != null && sp.has(Behavior.SHOCKWAVE) && shockCd <= 0 && distanceTo(target) < 6.0) {
            shockwave(sl, sp);
            shockCd = sp.boss() ? 90 : 160;
        }
        if (target != null && sp.has(Behavior.MELEE_BRUISER) && meleeCd <= 0 && distanceTo(target) < 2.6) {
            bruiserSlam(sl, target);
            meleeCd = 30;
        }
        if (target != null && sp.has(Behavior.SUMMONER) && summonCd <= 0) {
            if (summonAdds(sl, sp)) summonCd = sp.boss() ? 160 : 280;
        }
    }

    // ───────────────────────────────── abilities ─────────────────────────────────

    /** Poison/Frost/Wither auras + flesh-warping touch on nearby players; returns true if any hit. */
    private boolean applyAuras(ServerLevel sl, SpiritSpecies sp) {
        double radius = sp.has(Behavior.WITHER_TOUCH) ? 3.0 : 5.0 + getGrade() * 0.5;
        AABB box = getBoundingBox().inflate(radius);
        boolean any = false;
        for (Player p : sl.getEntitiesOfClass(Player.class, box)) {
            if (p.isCreative() || p.isSpectator()) continue;
            int amp = Math.max(0, getGrade() - 2);
            if (sp.has(Behavior.POISON_AURA)) {
                p.addEffect(new MobEffectInstance(MobEffects.POISON, 80, amp));
                if (getGrade() >= 4) p.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            }
            if (sp.has(Behavior.FROST_AURA)) {
                p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1 + amp));
                p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, amp));
            }
            if (sp.has(Behavior.WITHER_TOUCH)) {
                p.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, amp));
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            }
            if (sp.has(Behavior.LIFE_DRAIN)) heal(1.5f + getGrade() * 0.5f);
            any = true;
        }
        if (any) {
            sl.sendParticles(sp.has(Behavior.POISON_AURA) ? ParticleTypes.SNEEZE
                            : sp.has(Behavior.FROST_AURA) ? ParticleTypes.SNOWFLAKE : ParticleTypes.SOUL,
                    getX(), getY() + getBbHeight() * 0.5, getZ(), 18, radius * 0.4, 0.6, radius * 0.4, 0.01);
        }
        return any;
    }

    /** Hitscan lance of cursed energy (optionally igniting) along the line of sight to the target. */
    private void fireBlast(ServerLevel sl, SpiritSpecies sp, LivingEntity target) {
        Vec3 from = getEyePosition();
        Vec3 to = target.getEyePosition();
        Vec3 step = to.subtract(from).scale(1.0 / 14.0);
        Vec3 cur = from;
        boolean fire = sp.has(Behavior.FIRE_BLAST);
        for (int i = 0; i < 14; i++) {
            cur = cur.add(step);
            sl.sendParticles(fire ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.WITCH,
                    cur.x, cur.y, cur.z, 2, 0.04, 0.04, 0.04, 0.0);
        }
        float dmg = (float) (4.0 + getGrade() * 2.5);
        target.hurtServer(sl, sl.damageSources().magic(), dmg);
        if (fire) target.setRemainingFireTicks(80);
        if (sp.has(Behavior.LIFE_DRAIN)) heal(dmg * 0.4f);
        playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 0.6f);
    }

    /** Teleport to flank the target; returns true on a successful relocation. */
    private boolean blink(ServerLevel sl, LivingEntity target) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double ang = getRandom().nextDouble() * Math.PI * 2.0;
            double dist = 2.5 + getRandom().nextDouble() * 3.5;
            double nx = target.getX() + Math.cos(ang) * dist;
            double nz = target.getZ() + Math.sin(ang) * dist;
            BlockPos pos = BlockPos.containing(nx, target.getY(), nz);
            if (!sl.getBlockState(pos).isAir() || !sl.getBlockState(pos.above()).isAir()) continue;
            sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 24, 0.3, 0.6, 0.3, 0.4);
            teleportTo(nx, target.getY(), nz);
            sl.sendParticles(ParticleTypes.PORTAL, nx, target.getY() + 1, nz, 24, 0.3, 0.6, 0.3, 0.4);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 0.8f);
            return true;
        }
        return false;
    }

    /** Radial cursed-energy shockwave: knock back and wound nearby players. */
    private void shockwave(ServerLevel sl, SpiritSpecies sp) {
        double radius = 4.0 + getGrade() * 0.8;
        AABB box = getBoundingBox().inflate(radius);
        float dmg = (float) (3.0 + getGrade() * 1.8);
        for (Player p : sl.getEntitiesOfClass(Player.class, box)) {
            if (p.isCreative() || p.isSpectator()) continue;
            pushAway(p, 1.2, 0.55);
            p.hurtServer(sl, sl.damageSources().magic(), dmg);
        }
        sl.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + getBbHeight() * 0.5, getZ(), 1, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.4, getZ(), 40, radius * 0.4, 0.2, radius * 0.4, 0.05);
        playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.2f, 0.9f);
    }

    /** Heavy melee slam: extra knockback (and Wither for touch curses) on a nearby target. */
    private void bruiserSlam(ServerLevel sl, LivingEntity target) {
        pushAway(target, 0.85, 0.45);
        if (species().has(Behavior.WITHER_TOUCH)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, Math.max(0, getGrade() - 2)));
        }
        if (species().has(Behavior.LIFE_DRAIN)) heal(2.0f + getGrade());
        sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
    }

    /** Manifest a couple of weaker curses to fight alongside; capped by the local curse population. */
    private boolean summonAdds(ServerLevel sl, SpiritSpecies sp) {
        AABB box = getBoundingBox().inflate(18.0);
        long nearby = sl.getEntitiesOfClass(CursedSpiritEntity.class, box).size();
        if (nearby >= 7) return false;
        SpiritSpecies add = getGrade() >= 4 ? SpiritSpecies.SHADOW_IMP : SpiritSpecies.GRUDGE_LARVA;
        EntityType<CursedSpiritEntity> type = ModSpirits.typeFor(add);
        int count = 1 + getRandom().nextInt(2);
        boolean spawned = false;
        for (int i = 0; i < count; i++) {
            CursedSpiritEntity minion = type.create(sl, EntitySpawnReason.MOB_SUMMONED);
            if (minion == null) continue;
            minion.setPos(getX() + (getRandom().nextDouble() - 0.5) * 4, getY(), getZ() + (getRandom().nextDouble() - 0.5) * 4);
            CurseManager.manifest(minion, Math.max(1, getGrade() - 2));
            if (getTarget() != null) minion.setTarget(getTarget());
            sl.addFreshEntity(minion);
            spawned = true;
        }
        if (spawned) {
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 24, 1.5, 0.6, 1.5, 0.02);
            playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0f, 0.7f);
        }
        return spawned;
    }

    private void enrage(ServerLevel sl) {
        enraged = true;
        addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, 1, false, false));
        addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 1, false, false));
        addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + getBbHeight(), getZ(), 30, 0.6, 0.6, 0.6, 0.1);
        sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.4, getZ(), 40, 0.6, 0.4, 0.6, 0.06);
        playSound(SoundEvents.WITHER_SPAWN, 1.0f, 1.4f);
        if (bossBar != null) bossBar.setColor(BossEvent.BossBarColor.RED);
    }

    private void pushAway(LivingEntity victim, double horizontal, double vertical) {
        double dx = victim.getX() - getX();
        double dz = victim.getZ() - getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 1.0e-4) {
            victim.push(dx / len * horizontal, vertical, dz / len * horizontal);
            victim.hurtMarked = true;
        }
    }

    // ───────────────────────────────── boss bar ─────────────────────────────────

    private void updateBossBar(ServerLevel sl) {
        if (bossBar == null) {
            Component name = getCustomName() != null ? getCustomName()
                    : Component.literal(species().displayName()).withStyle(ChatFormatting.DARK_RED);
            bossBar = new ServerBossEvent(getUUID(), name, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
            bossBar.setDarkenScreen(true);
        }
        bossBar.setProgress(Math.max(0f, Math.min(1f, getHealth() / getMaxHealth())));
        AABB range = getBoundingBox().inflate(42.0);
        java.util.Set<ServerPlayer> viewers = new java.util.HashSet<>(sl.getEntitiesOfClass(ServerPlayer.class, range));
        for (ServerPlayer p : new java.util.ArrayList<>(bossBar.getPlayers())) {
            if (!viewers.contains(p)) bossBar.removePlayer(p);
        }
        for (ServerPlayer p : viewers) bossBar.addPlayer(p);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (bossBar != null) bossBar.removeAllPlayers();
        super.remove(reason);
    }
}
