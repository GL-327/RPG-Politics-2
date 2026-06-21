package com.political.curse;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A true Curse: a custom cursed-spirit mob with its own model + texture (see the client
 * {@code CurseModels}/{@code CurseRenderer}) rather than a re-skinned vanilla monster. It hunts
 * like an undead but never burns in daylight and never converts, and it carries a 1..5 grade
 * that scales its size, health and damage. Animations come from the humanoid rig (walk/attack).
 */
public class CurseEntity extends Zombie {

    private int grade = 1;

    public CurseEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    /** A Curse is meaner and tougher than a plain zombie even at grade 1. */
    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = Math.max(1, Math.min(5, grade));
    }

    @Override
    public boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean convertsInWater() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("CurseGrade", grade);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.grade = Math.max(1, Math.min(5, in.getIntOr("CurseGrade", 1)));
    }
}
