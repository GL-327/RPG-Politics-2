package com.political.world;

import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * A captured list of block placements produced by the generators while running in
 * "deferred" mode. {@link SettlementManager} drains these a few thousand per tick so
 * large structures rise smoothly instead of freezing the server for a couple of seconds.
 */
public final class BuildBuffer {

    public static final class Op {
        public final int x;
        public final int y;
        public final int z;
        public final BlockState state;

        Op(int x, int y, int z, BlockState state) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.state = state;
        }
    }

    public final List<Op> ops = new ArrayList<>();

    void add(int x, int y, int z, BlockState state) {
        ops.add(new Op(x, y, z, state));
    }

    public int size() {
        return ops.size();
    }
}
