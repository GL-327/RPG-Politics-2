package com.political.curse;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/**
 * A true Curse: a custom cursed-spirit mob (with its own texture) rather than a re-skinned
 * vanilla monster. It behaves like an undead hunter but never burns in daylight and never
 * "drowns" into another form \u2014 a curse is a curse wherever it manifests.
 */
public class CurseEntity extends Zombie {

    public CurseEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean convertsInWater() {
        return false;
    }
}
