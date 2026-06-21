package com.political.expansion2.mobs;

public enum MobRole2 {
    HOSTILE,
    NEUTRAL,
    SKITTISH,
    MINIBOSS,
    BOSS;

    public boolean isAggressive() {
        return this == HOSTILE || this == MINIBOSS || this == BOSS;
    }

    public boolean isBossLike() {
        return this == MINIBOSS || this == BOSS;
    }
}
