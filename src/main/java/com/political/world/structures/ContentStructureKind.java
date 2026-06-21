package com.political.world.structures;

import java.util.Locale;

/**
 * The content workstream's own small surface-structure set — original layouts authored in
 * {@link ContentStructures} and built through the shared {@link com.political.world.BuildBuffer}
 * pipeline. Jigsaw/template-pool layout ideas were studied from Towns &amp; Towers and Dungeons
 * Arise, but every layout here is hand-authored from {@link com.political.world.Build} primitives
 * (no copied NBT/templates).
 *
 * <p>Each kind borrows an existing {@link StructureType} only for loot-table + bookkeeping wiring
 * (so no new loot tables are required); the geometry is independent.
 */
public enum ContentStructureKind {

    /** A roadside civic waystone: a runed pillar, a lantern and a small offering cache. */
    WAYSTONE_SHRINE("waystone_shrine", "Waystone Shrine", StructureType.ELECTION_HALL_RUIN),

    /** A timber ranger's outpost: a one-room cabin with a watch banner and a friendly ranger. */
    RANGER_OUTPOST("ranger_outpost", "Ranger Outpost", StructureType.HERO_OUTPOST),

    /** A leyline nexus: an arcane stone ring crowned with amethyst, haunted by a faint curse. */
    LEYLINE_NEXUS("leyline_nexus", "Leyline Nexus", StructureType.MAGE_TOWER);

    public final String id;
    public final String display;
    /** Existing archetype reused purely for its loot table + site bookkeeping. */
    public final StructureType loot;

    ContentStructureKind(String id, String display, StructureType loot) {
        this.id = id;
        this.display = display;
        this.loot = loot;
    }

    public static ContentStructureKind byId(String id) {
        if (id == null) return null;
        String key = id.toLowerCase(Locale.ROOT);
        for (ContentStructureKind k : values()) if (k.id.equals(key)) return k;
        return null;
    }
}
