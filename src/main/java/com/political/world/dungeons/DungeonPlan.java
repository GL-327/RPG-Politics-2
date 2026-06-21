package com.political.world.dungeons;

import com.political.world.BuildBuffer;

import java.util.ArrayList;
import java.util.List;

/** Captured generation output: block buffer plus post-placement hooks. */
public final class DungeonPlan {

    public enum PostKind {
        CHEST_COMMON, CHEST_UNCOMMON, CHEST_RARE, CHEST_EPIC, CHEST_BOSS,
        SPAWNER, ARROW_TRAP, SOUL_FIRE_TRAP, MOB, BOSS, WATER_FILL
    }

    public static final class PostOp {
        public final PostKind kind;
        public final int x;
        public final int y;
        public final int z;
        public final String mobId;

        PostOp(PostKind kind, int x, int y, int z, String mobId) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.mobId = mobId;
        }

        static PostOp of(PostKind kind, int x, int y, int z) {
            return new PostOp(kind, x, y, z, null);
        }

        static PostOp mob(int x, int y, int z, String mobId) {
            return new PostOp(PostKind.MOB, x, y, z, mobId);
        }

        static PostOp spawner(int x, int y, int z, String mobId) {
            return new PostOp(PostKind.SPAWNER, x, y, z, mobId);
        }
    }

    public final BuildBuffer buffer;
    public final DungeonSite site;
    public final List<PostOp> postOps = new ArrayList<>();

    public DungeonPlan(BuildBuffer buffer, DungeonSite site) {
        this.buffer = buffer;
        this.site = site;
    }
}
