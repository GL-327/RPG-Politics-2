package com.political.expansion2.ranged;

public record CastEffects(
        ParticleKind particle,
        int fireTicks,
        double aimRadius,
        double chainRadius,
        float chainMult,
        double coneTightness,
        int slowAmp, int slowTicks,
        int poisonAmp, int poisonTicks,
        int witherAmp, int witherTicks,
        int weaknessAmp, int weaknessTicks,
        int blindnessTicks,
        float healSelf, float healAllies,
        float drainPerHit,
        float selfHealthCost,
        boolean lightning,
        boolean launchAway,
        boolean launchFrom,
        SummonKind summon,
        int summonCount
) {
    public static CastEffects of(ParticleKind particle, int fireTicks, double aimRadius, double chainRadius,
                                 float chainMult, double coneTightness,
                                 int[] slow, int[] poison, int[] wither, int[] weakness,
                                 int blindnessTicks, float healSelf, float healAllies, float drainPerHit,
                                 float selfHealthCost, boolean lightning, boolean launchAway, boolean launchFrom,
                                 SummonKind summon, int summonCount) {
        return new CastEffects(particle, fireTicks, aimRadius, chainRadius, chainMult, coneTightness,
                slow[0], slow[1], poison[0], poison[1], wither[0], wither[1], weakness[0], weakness[1],
                blindnessTicks, healSelf, healAllies, drainPerHit, selfHealthCost,
                lightning, launchAway, launchFrom, summon, summonCount);
    }
}
