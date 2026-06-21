package com.political.world.structures;

import com.political.world.BuildBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Captured generation output for one surface structure: the deferred block buffer plus a list
 * of post-placement hooks (chest loot, spawners, mob/villager/trader/cursed-spirit spawns) that
 * run once the structure has finished streaming into the world.
 */
public final class StructurePlan {

    public enum PostKind { CHEST, SPAWNER, MOB, VILLAGER, TRADER, CURSED_SPIRIT }

    public static final class PostOp {
        public final PostKind kind;
        public final int x;
        public final int y;
        public final int z;
        /** Mob id (MOB/SPAWNER) or loot table id (CHEST). */
        public final String arg;
        /** Cursed spirit grade (CURSED_SPIRIT). */
        public final int grade;

        PostOp(PostKind kind, int x, int y, int z, String arg, int grade) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.arg = arg;
            this.grade = grade;
        }

        static PostOp chest(int x, int y, int z, String lootTable) {
            return new PostOp(PostKind.CHEST, x, y, z, lootTable, 0);
        }

        static PostOp spawner(int x, int y, int z, String mobId) {
            return new PostOp(PostKind.SPAWNER, x, y, z, mobId, 0);
        }

        static PostOp mob(int x, int y, int z, String mobId) {
            return new PostOp(PostKind.MOB, x, y, z, mobId, 0);
        }

        static PostOp villager(int x, int y, int z) {
            return new PostOp(PostKind.VILLAGER, x, y, z, null, 0);
        }

        static PostOp trader(int x, int y, int z) {
            return new PostOp(PostKind.TRADER, x, y, z, null, 0);
        }

        static PostOp cursedSpirit(int x, int y, int z, int grade) {
            return new PostOp(PostKind.CURSED_SPIRIT, x, y, z, null, grade);
        }
    }

    public final BuildBuffer buffer;
    public final StructureSite site;
    public final List<PostOp> postOps = new ArrayList<>();

    public StructurePlan(BuildBuffer buffer, StructureSite site) {
        this.buffer = buffer;
        this.site = site;
    }
}
