package com.political.bounty;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;

/** Bounty Hunt disciplines (original rebrand of a slayer-line concept). */
public enum BountyType {
    UNDEAD("Undead Purge", EntityTypes.ZOMBIE),
    ARACHNID("Arachnid Cull", EntityTypes.SPIDER),
    INFERNAL("Infernal Hunt", EntityTypes.BLAZE),
    VOID("Void Reckoning", EntityTypes.ENDERMAN),
    DECAYED("Decayed Watch", EntityTypes.SKELETON);

    public final String displayName;
    public final EntityType<? extends Mob> entityType;

    BountyType(String displayName, EntityType<? extends Mob> entityType) {
        this.displayName = displayName;
        this.entityType = entityType;
    }

    public static BountyType byId(String id) {
        try {
            return valueOf(id.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
