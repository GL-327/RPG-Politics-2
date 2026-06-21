package com.political.expansion2.curses;

import com.political.curse.CurseEntity;
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

/** Shared entity for every {@link SpiritSpecies2}. */
public class CursedSpirit2Entity extends CurseEntity {

    private SpiritSpecies2 species;
    private ServerBossEvent bossBar;
    private boolean enraged;
    private boolean replicated;

    private int blastCd, auraCd, teleportCd, summonCd, shockCd, meleeCd;
    private int domainCd, boltCd, disasterCd, maximumCd, shikigamiCd, necromancyCd;
    private int rainbowCd, gravityCd, chainCd, voiceCd;

    public CursedSpirit2Entity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public SpiritSpecies2 species() {
        if (species == null) species = CurseSpirits2.speciesFor(getType());
        return species;
    }

    /**
     * Same cursed-energy perception gate as the base {@link CurseEntity}, except boss spirits radiate
     * so much cursed energy that they are visible to everyone (so the boss bar always makes sense).
     */
    @Override
    public boolean isInvisibleTo(Player player) {
        SpiritSpecies2 sp = species();
        if (sp != null && sp.boss()) return baseInvisibleTo(player);
        return super.isInvisibleTo(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel sl)) return;
        SpiritSpecies2 sp = species();
        if (sp == null) return;

        if (sp.has(Behavior2.FIRE_IMMUNE)) setRemainingFireTicks(0);
        if (sp.has(Behavior2.REGEN) && tickCount % 40 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0f + getGrade() * 0.5f);
        }
        if (sp.has(Behavior2.ENRAGE) && !enraged && getHealth() < getMaxHealth() * 0.30f) enrage(sl);
        if (sp.has(Behavior2.SWARM_REPLICATE) && !replicated && getHealth() < getMaxHealth() * 0.50f) replicate(sl, sp);
        if (sp.boss()) updateBossBar(sl);

        LivingEntity target = getTarget();
        tickCooldowns();

        if ((sp.has(Behavior2.POISON_AURA) || sp.has(Behavior2.FROST_AURA) || sp.has(Behavior2.WITHER_TOUCH)
                || sp.has(Behavior2.CURSE_SEAL) || sp.has(Behavior2.BLINDNESS_CURSE)) && auraCd <= 0) {
            if (applyAuras(sl, sp)) auraCd = 40;
        }
        if (sp.has(Behavior2.VOICE_CURSE) && voiceCd <= 0 && voiceCurse(sl)) voiceCd = 60;
        if (sp.has(Behavior2.DOMAIN_FIELD) && domainCd <= 0 && domainField(sl)) domainCd = sp.boss() ? 100 : 180;
        if (target != null && (sp.has(Behavior2.RANGED_BLAST) || sp.has(Behavior2.FIRE_BLAST))
                && blastCd <= 0 && distanceTo(target) <= 22.0 && getSensing().hasLineOfSight(target)) {
            fireBlast(sl, sp, target);
            blastCd = sp.boss() ? 30 : 50;
        }
        if (target != null && sp.has(Behavior2.CURSED_BOLT) && boltCd <= 0
                && distanceTo(target) <= 18.0 && getSensing().hasLineOfSight(target)) {
            cursedBolt(sl, target);
            boltCd = sp.boss() ? 40 : 70;
        }
        if (target != null && sp.has(Behavior2.RAINBOW_BEAM) && rainbowCd <= 0
                && distanceTo(target) <= 24.0 && getSensing().hasLineOfSight(target)) {
            rainbowBeam(sl, target);
            rainbowCd = sp.boss() ? 50 : 90;
        }
        if (target != null && sp.has(Behavior2.TELEPORT) && teleportCd <= 0
                && (distanceTo(target) > 8.0 || getRandom().nextFloat() < 0.05f) && blink(sl, target)) {
            teleportCd = sp.boss() ? 60 : 110;
        }
        if (target != null && sp.has(Behavior2.SHOCKWAVE) && shockCd <= 0 && distanceTo(target) < 6.0) {
            shockwave(sl);
            shockCd = sp.boss() ? 90 : 160;
        }
        if (target != null && sp.has(Behavior2.MAXIMUM_BURST) && maximumCd <= 0 && distanceTo(target) < 10.0) {
            maximumBurst(sl);
            maximumCd = sp.boss() ? 120 : 200;
        }
        if (target != null && sp.has(Behavior2.DISASTER_ERUPT) && disasterCd <= 0 && distanceTo(target) < 8.0) {
            disasterErupt(sl);
            disasterCd = sp.boss() ? 80 : 140;
        }
        if (target != null && (sp.has(Behavior2.MELEE_BRUISER) || sp.has(Behavior2.ARMAMENT_FORM))
                && meleeCd <= 0 && distanceTo(target) < 2.8) {
            bruiserSlam(sl, target, sp);
            meleeCd = sp.has(Behavior2.ARMAMENT_FORM) ? 22 : 30;
        }
        if (target != null && sp.has(Behavior2.GRAVITY_PULL) && gravityCd <= 0 && distanceTo(target) < 12.0) {
            gravityPull(sl);
            gravityCd = 80;
        }
        if (sp.has(Behavior2.CHAIN_CURSE) && chainCd <= 0 && chainCurse(sl)) chainCd = 100;
        if (target != null && sp.has(Behavior2.SUMMONER) && summonCd <= 0 && summonAdds(sl, sp)) {
            summonCd = sp.boss() ? 160 : 280;
        }
        if (target != null && sp.has(Behavior2.SHIKIGAMI_CALL) && shikigamiCd <= 0 && shikigamiCall(sl)) {
            shikigamiCd = sp.boss() ? 140 : 220;
        }
        if (target != null && sp.has(Behavior2.NECROMANCY_RISE) && necromancyCd <= 0 && necromancyRise(sl)) {
            necromancyCd = sp.boss() ? 150 : 240;
        }
    }

    private void tickCooldowns() {
        if (blastCd > 0) blastCd--; if (auraCd > 0) auraCd--; if (teleportCd > 0) teleportCd--;
        if (summonCd > 0) summonCd--; if (shockCd > 0) shockCd--; if (meleeCd > 0) meleeCd--;
        if (domainCd > 0) domainCd--; if (boltCd > 0) boltCd--; if (disasterCd > 0) disasterCd--;
        if (maximumCd > 0) maximumCd--; if (shikigamiCd > 0) shikigamiCd--; if (necromancyCd > 0) necromancyCd--;
        if (rainbowCd > 0) rainbowCd--; if (gravityCd > 0) gravityCd--; if (chainCd > 0) chainCd--;
        if (voiceCd > 0) voiceCd--;
    }

    private boolean applyAuras(ServerLevel sl, SpiritSpecies2 sp) {
        double radius = sp.has(Behavior2.WITHER_TOUCH) ? 3.0 : 5.0 + getGrade() * 0.5;
        AABB box = getBoundingBox().inflate(radius);
        boolean any = false;
        for (Player p : sl.getEntitiesOfClass(Player.class, box)) {
            if (p.isCreative() || p.isSpectator()) continue;
            int amp = Math.max(0, getGrade() - 2);
            if (sp.has(Behavior2.POISON_AURA)) {
                p.addEffect(new MobEffectInstance(MobEffects.POISON, 80, amp));
                if (getGrade() >= 4) p.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            }
            if (sp.has(Behavior2.FROST_AURA)) {
                p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1 + amp));
                p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, amp));
            }
            if (sp.has(Behavior2.WITHER_TOUCH)) {
                p.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, amp));
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            }
            if (sp.has(Behavior2.CURSE_SEAL)) {
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1 + amp));
                p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, amp));
            }
            if (sp.has(Behavior2.BLINDNESS_CURSE)) {
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
            }
            if (sp.has(Behavior2.LIFE_DRAIN)) heal(1.5f + getGrade() * 0.5f);
            any = true;
        }
        if (any) {
            sl.sendParticles(sp.has(Behavior2.POISON_AURA) ? ParticleTypes.SNEEZE
                            : sp.has(Behavior2.FROST_AURA) ? ParticleTypes.SNOWFLAKE
                            : sp.has(Behavior2.BLINDNESS_CURSE) ? ParticleTypes.SMOKE : ParticleTypes.SOUL,
                    getX(), getY() + getBbHeight() * 0.5, getZ(), 18, radius * 0.4, 0.6, radius * 0.4, 0.01);
        }
        return any;
    }

    private boolean voiceCurse(ServerLevel sl) {
        AABB box = getBoundingBox().inflate(8.0);
        boolean any = false;
        for (Player p : sl.getEntitiesOfClass(Player.class, box)) {
            if (p.isCreative() || p.isSpectator()) continue;
            p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, Math.max(0, getGrade() - 2)));
            any = true;
        }
        if (any) {
            sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1, getZ(), 20, 1.5, 0.5, 1.5, 0.02);
            playSound(SoundEvents.WARDEN_AGITATED, 0.8f, 0.6f);
        }
        return any;
    }

    private boolean domainField(ServerLevel sl) {
        double radius = 6.0 + getGrade();
        AABB box = getBoundingBox().inflate(radius);
        boolean any = false;
        for (Player p : sl.getEntitiesOfClass(Player.class, box)) {
            if (p.isCreative() || p.isSpectator()) continue;
            p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
            any = true;
        }
        if (any) {
            sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 0.2, getZ(), 60, radius * 0.5, 0.3, radius * 0.5, 0.5);
            playSound(SoundEvents.BEACON_AMBIENT, 0.6f, 0.4f);
        }
        return any;
    }

    private void fireBlast(ServerLevel sl, SpiritSpecies2 sp, LivingEntity target) {
        Vec3 from = getEyePosition(), to = target.getEyePosition();
        Vec3 step = to.subtract(from).scale(1.0 / 14.0), cur = from;
        boolean fire = sp.has(Behavior2.FIRE_BLAST);
        for (int i = 0; i < 14; i++) {
            cur = cur.add(step);
            sl.sendParticles(fire ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.WITCH,
                    cur.x, cur.y, cur.z, 2, 0.04, 0.04, 0.04, 0.0);
        }
        float dmg = (float) (4.0 + getGrade() * 2.5);
        target.hurtServer(sl, sl.damageSources().magic(), dmg);
        if (fire) target.setRemainingFireTicks(80);
        if (sp.has(Behavior2.LIFE_DRAIN)) heal(dmg * 0.4f);
        playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 0.6f);
    }

    private void cursedBolt(ServerLevel sl, LivingEntity target) {
        Vec3 from = getEyePosition(), to = target.getEyePosition();
        Vec3 step = to.subtract(from).scale(1.0 / 10.0), cur = from;
        for (int i = 0; i < 10; i++) {
            cur = cur.add(step);
            sl.sendParticles(ParticleTypes.SOUL, cur.x, cur.y, cur.z, 3, 0.06, 0.06, 0.06, 0.01);
        }
        target.hurtServer(sl, sl.damageSources().magic(), (float) (3.0 + getGrade() * 2.0));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        playSound(SoundEvents.GHAST_SHOOT, 0.7f, 1.2f);
    }

    private void rainbowBeam(ServerLevel sl, LivingEntity target) {
        Vec3 from = getEyePosition(), to = target.getEyePosition();
        Vec3 step = to.subtract(from).scale(1.0 / 16.0), cur = from;
        var colors = new net.minecraft.core.particles.ParticleOptions[]{
                ParticleTypes.WITCH, ParticleTypes.FLAME, ParticleTypes.SOUL_FIRE_FLAME,
                ParticleTypes.END_ROD, ParticleTypes.SOUL
        };
        for (int i = 0; i < 16; i++) {
            cur = cur.add(step);
            sl.sendParticles(colors[i % colors.length], cur.x, cur.y, cur.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
        target.hurtServer(sl, sl.damageSources().magic(), (float) (6.0 + getGrade() * 3.0));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        playSound(SoundEvents.BEACON_POWER_SELECT, 1.0f, 1.5f);
    }

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

    private void shockwave(ServerLevel sl) {
        double radius = 4.0 + getGrade() * 0.8;
        float dmg = (float) (3.0 + getGrade() * 1.8);
        for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius))) {
            if (p.isCreative() || p.isSpectator()) continue;
            pushAway(p, 1.2, 0.55);
            p.hurtServer(sl, sl.damageSources().magic(), dmg);
        }
        sl.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + getBbHeight() * 0.5, getZ(), 1, 0, 0, 0, 0);
        playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.2f, 0.9f);
    }

    private void maximumBurst(ServerLevel sl) {
        double radius = 7.0 + getGrade();
        float dmg = (float) (8.0 + getGrade() * 3.5);
        for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius))) {
            if (p.isCreative() || p.isSpectator()) continue;
            pushAway(p, 1.5, 0.7);
            p.hurtServer(sl, sl.damageSources().magic(), dmg);
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
        }
        sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 3, 0.5, 0.5, 0.5, 0);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5f, 0.7f);
    }

    private void disasterErupt(ServerLevel sl) {
        double radius = 5.0 + getGrade() * 0.6;
        float dmg = (float) (5.0 + getGrade() * 2.2);
        for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius))) {
            if (p.isCreative() || p.isSpectator()) continue;
            pushAway(p, 1.0, 0.4);
            p.hurtServer(sl, sl.damageSources().magic(), dmg);
            p.setRemainingFireTicks(60);
        }
        sl.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.3, getZ(), 30, radius * 0.4, 0.2, radius * 0.4, 0.02);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 0.9f);
    }

    private void bruiserSlam(ServerLevel sl, LivingEntity target, SpiritSpecies2 sp) {
        pushAway(target, sp.has(Behavior2.ARMAMENT_FORM) ? 1.1 : 0.85, sp.has(Behavior2.ARMAMENT_FORM) ? 0.55 : 0.45);
        if (sp.has(Behavior2.WITHER_TOUCH)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, Math.max(0, getGrade() - 2)));
        }
        if (sp.has(Behavior2.ARMAMENT_FORM)) {
            target.hurtServer(sl, sl.damageSources().mobAttack(this), (float) (2.0 + getGrade()));
        }
        if (sp.has(Behavior2.LIFE_DRAIN)) heal(2.0f + getGrade());
        sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
    }

    private void gravityPull(ServerLevel sl) {
        for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(10.0))) {
            if (p.isCreative() || p.isSpectator()) continue;
            double dx = getX() - p.getX(), dz = getZ() - p.getZ(), len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.5) p.push(dx / len * 0.35, 0.08, dz / len * 0.35);
        }
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1, getZ(), 30, 2.0, 0.5, 2.0, 0.05);
    }

    private boolean chainCurse(ServerLevel sl) {
        var players = sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(12.0));
        if (players.size() < 2) return false;
        int amp = Math.max(0, getGrade() - 2);
        players.get(0).addEffect(new MobEffectInstance(MobEffects.WITHER, 60, amp));
        players.get(1).addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, amp));
        sl.sendParticles(ParticleTypes.WITCH, getX(), getY() + 1, getZ(), 20, 1.0, 0.5, 1.0, 0.02);
        return true;
    }

    private boolean summonAdds(ServerLevel sl, SpiritSpecies2 sp) {
        if (sl.getEntitiesOfClass(CursedSpirit2Entity.class, getBoundingBox().inflate(18.0)).size() >= 9) return false;
        SpiritSpecies2 add = CurseSpirits2.randomForGrade(Math.max(1, getGrade() - 2));
        EntityType<CursedSpirit2Entity> type = CurseSpirits2.typeFor(add);
        int count = 1 + getRandom().nextInt(sp.boss() ? 3 : 2);
        boolean spawned = false;
        for (int i = 0; i < count; i++) {
            CursedSpirit2Entity minion = type.create(sl, EntitySpawnReason.MOB_SUMMONED);
            if (minion == null) continue;
            minion.setPos(getX() + (getRandom().nextDouble() - 0.5) * 4, getY(), getZ() + (getRandom().nextDouble() - 0.5) * 4);
            CurseSpirits2.manifest(minion, Math.max(1, getGrade() - 2));
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

    private boolean shikigamiCall(ServerLevel sl) {
        SpiritSpecies2 pick = getRandom().nextBoolean() ? SpiritSpecies2.SHIKIGAMI_FOX : SpiritSpecies2.SHIKIGAMI_OWL;
        return spawnMinion(sl, pick, 1);
    }

    private boolean necromancyRise(ServerLevel sl) {
        return spawnMinion(sl, SpiritSpecies2.MAGGOT_HUSK, 1 + getRandom().nextInt(2));
    }

    private boolean spawnMinion(ServerLevel sl, SpiritSpecies2 add, int count) {
        if (sl.getEntitiesOfClass(CursedSpirit2Entity.class, getBoundingBox().inflate(18.0)).size() >= 10) return false;
        EntityType<CursedSpirit2Entity> type = CurseSpirits2.typeFor(add);
        boolean spawned = false;
        for (int i = 0; i < count; i++) {
            CursedSpirit2Entity minion = type.create(sl, EntitySpawnReason.MOB_SUMMONED);
            if (minion == null) continue;
            minion.setPos(getX() + (getRandom().nextDouble() - 0.5) * 5, getY(), getZ() + (getRandom().nextDouble() - 0.5) * 5);
            CurseSpirits2.manifest(minion, Math.max(1, getGrade() - 2));
            if (getTarget() != null) minion.setTarget(getTarget());
            sl.addFreshEntity(minion);
            spawned = true;
        }
        if (spawned) playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 0.8f, 0.5f);
        return spawned;
    }

    private void replicate(ServerLevel sl, SpiritSpecies2 sp) {
        replicated = true;
        EntityType<CursedSpirit2Entity> type = CurseSpirits2.typeFor(sp);
        CursedSpirit2Entity copy = type.create(sl, EntitySpawnReason.MOB_SUMMONED);
        if (copy == null) return;
        copy.setPos(getX() + 1, getY(), getZ());
        CurseSpirits2.manifest(copy, getGrade());
        if (getTarget() != null) copy.setTarget(getTarget());
        sl.addFreshEntity(copy);
        sl.sendParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 20, 0.5, 0.5, 0.5, 0.05);
    }

    private void enrage(ServerLevel sl) {
        enraged = true;
        addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, 1, false, false));
        addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 1, false, false));
        addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.4, getZ(), 40, 0.6, 0.4, 0.6, 0.06);
        playSound(SoundEvents.WITHER_SPAWN, 1.0f, 1.4f);
        if (bossBar != null) bossBar.setColor(BossEvent.BossBarColor.RED);
    }

    private void pushAway(LivingEntity victim, double horizontal, double vertical) {
        double dx = victim.getX() - getX(), dz = victim.getZ() - getZ(), len = Math.sqrt(dx * dx + dz * dz);
        if (len > 1.0e-4) { victim.push(dx / len * horizontal, vertical, dz / len * horizontal); victim.hurtMarked = true; }
    }

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
