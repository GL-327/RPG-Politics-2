package com.political.curse.technique;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Insertion-ordered registry of every {@link CursedTechnique}. Populated once by
 * {@link CursedTechniques#bootstrap()} during common init.
 */
public final class TechniqueRegistry {

    private static final Map<String, CursedTechnique> BY_ID = new LinkedHashMap<>();

    private TechniqueRegistry() {}

    public static void register(CursedTechnique technique) {
        BY_ID.put(technique.id(), technique);
    }

    public static CursedTechnique byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    public static Collection<CursedTechnique> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static boolean isReady() {
        return !BY_ID.isEmpty();
    }

    /** Techniques unlocked at or below the given sorcerer grade, in registry order. */
    public static List<CursedTechnique> knownFor(int grade) {
        List<CursedTechnique> out = new ArrayList<>();
        for (CursedTechnique t : BY_ID.values()) {
            if (t.requiredGrade() <= grade) out.add(t);
        }
        return out;
    }
}
