package com.political.curse;

import com.political.curse.energy.CursedEnergy;
import com.political.vfx.VfxElement;
import com.political.vfx.VfxHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A custom cursed-energy ranged attack for {@link CurseEntity}: when a curse has a clear line of sight
 * to its target it lobs a hex bolt of cursed energy, dealing grade-scaled damage and a brief weakness,
 * with a per-curse cooldown. Implemented as a vanilla {@link Goal} so it co-operates with the inherited
 * zombie path/melee/target goals.
 */
public class CurseHexBoltGoal extends Goal {

    private final CurseEntity curse;
    private int nextUseTick;

    public CurseHexBoltGoal(CurseEntity curse) {
        this.curse = curse;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = curse.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (curse.tickCount < nextUseTick) return false;
        // Don't bother foes who can't even perceive the curse.
        if (target instanceof net.minecraft.world.entity.player.Player p && !CursedEnergy.canPerceive(p, curse.getGrade())) {
            return false;
        }
        return curse.distanceToSqr(target) <= 18.0 * 18.0 && curse.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = curse.getTarget();
        if (target == null || !(curse.level() instanceof ServerLevel level)) return;

        curse.getLookControl().setLookAt(target, 30.0f, 30.0f);
        Vec3 origin = curse.getEyePosition();
        Vec3 to = target.getEyePosition();
        Vec3 dir = to.subtract(origin).normalize();
        VfxHelper.elementBeam(level, VfxElement.VOID, origin, dir, origin.distanceTo(to));
        VfxHelper.hitSpark(level, target, VfxElement.VOID);

        float dmg = 2.5f + curse.getGrade() * 1.6f;
        target.hurtServer(level, level.damageSources().mobAttack(curse), dmg);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        curse.playSound(SoundEvents.EVOKER_CAST_SPELL, 0.7f, 1.3f);

        nextUseTick = curse.tickCount + 70 + curse.getRandom().nextInt(50);
    }
}
