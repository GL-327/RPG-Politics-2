package com.political.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side mirror of active domain expansions, fed by {@code DomainSyncS2C}. */
public final class DomainClientState {

    public record ActiveDomain(
            String domainId,
            double centerX,
            double centerY,
            double centerZ,
            float radius,
            int elementOrdinal,
            int ticksLeft) {}

    /** casterEntityId -> active domain snapshot (empty domainId means remove). */
    public static final Map<Integer, ActiveDomain> ACTIVE = new ConcurrentHashMap<>();

    /** Local player's currently active domain, if any. */
    public static volatile ActiveDomain localActive;

    private DomainClientState() {}
}
