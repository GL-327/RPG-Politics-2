package com.political.curse.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Insertion-ordered registry of every {@link CursedDomain}. Populated by {@link Domains#bootstrap()}. */
public final class DomainRegistry {

    private static final Map<String, CursedDomain> BY_ID = new LinkedHashMap<>();

    private DomainRegistry() {}

    static void register(CursedDomain domain) {
        BY_ID.put(domain.id(), domain);
    }

    public static CursedDomain byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    public static Collection<CursedDomain> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static boolean isReady() {
        return !BY_ID.isEmpty();
    }

    /** Domains unlocked at or below the given sorcerer grade, in registry order. */
    public static List<CursedDomain> knownFor(int grade) {
        List<CursedDomain> out = new ArrayList<>();
        for (CursedDomain d : BY_ID.values()) {
            if (d.requiredGrade() <= grade) out.add(d);
        }
        return out;
    }

    /** The most demanding domain the grade unlocks (used when the player asks for "best available"). */
    public static CursedDomain bestFor(int grade) {
        CursedDomain best = null;
        for (CursedDomain d : BY_ID.values()) {
            if (d.requiredGrade() <= grade && (best == null || d.requiredGrade() >= best.requiredGrade())) {
                best = d;
            }
        }
        return best;
    }
}
