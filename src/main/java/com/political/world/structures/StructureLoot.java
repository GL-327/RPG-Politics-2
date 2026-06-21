package com.political.world.structures;

import com.political.RpgPoliticsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Random;

/**
 * Applies post-generation hooks for surface structures: fills chests from the
 * {@code chests/structures/*} loot tables, configures spawners, and brings the site to life
 * with ambient hostile mobs, friendly villagers/traders, or cursed spirits — all via the
 * existing public spawn APIs ({@code ExpansionMobs}, {@code ExpansionMobs2}, {@code CurseSpirits2}).
 */
public final class StructureLoot {

    private static final Random RNG = new Random();

    private StructureLoot() {}

    public static void applyPostOps(ServerLevel level, StructurePlan plan) {
        for (StructurePlan.PostOp op : plan.postOps) {
            switch (op.kind) {
                case CHEST -> fillChest(level, op.x, op.y, op.z, op.arg);
                case SPAWNER -> configureSpawner(level, op.x, op.y, op.z, op.arg);
                case MOB -> spawnMob(level, op.x, op.y, op.z, op.arg);
                case VILLAGER -> spawnVillager(level, op.x, op.y, op.z);
                case TRADER -> spawnTrader(level, op.x, op.y, op.z);
                case CURSED_SPIRIT -> spawnCursedSpirit(level, op.x, op.y, op.z, op.grade);
            }
        }
    }

    private static void fillChest(ServerLevel level, int x, int y, int z, String table) {
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof ChestBlockEntity chest)) return;
        chest.clearContent();
        try {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                    Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, "chests/structures/" + table));
            LootTable lt = level.getServer().reloadableRegistries().getLootTable(key);
            LootParams params = new LootParams.Builder(level).create(LootContextParamSets.CHEST);
            lt.fill(chest, params, level.getRandom().nextLong());
        } catch (Exception e) {
            // Graceful fallback so a missing/renamed table never breaks generation.
            addFallback(chest);
        }
    }

    private static void addFallback(ChestBlockEntity chest) {
        ItemStack coin = modItem("coin_pouch");
        if (coin.isEmpty()) coin = new ItemStack(net.minecraft.world.item.Items.EMERALD);
        coin.setCount(1 + RNG.nextInt(3));
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).isEmpty()) { chest.setItem(i, coin); return; }
        }
    }

    private static void configureSpawner(ServerLevel level, int x, int y, int z, String mobId) {
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof SpawnerBlockEntity spawner)) return;
        var spec = com.political.expansion.mobs.ExpansionMobs.specById(mobId);
        if (spec != null && spec.type != null) {
            spawner.setEntityId(spec.type, level.getRandom());
            return;
        }
        var spec2 = com.political.expansion2.mobs.ExpansionMobs2.specById(mobId);
        if (spec2 != null && spec2.type != null) {
            spawner.setEntityId(spec2.type, level.getRandom());
            return;
        }
        spawner.setEntityId(EntityTypes.ZOMBIE, level.getRandom());
    }

    private static void spawnMob(ServerLevel level, int x, int y, int z, String mobId) {
        if (mobId == null) return;
        var mob = com.political.expansion.mobs.ExpansionMobs.spawnById(level, new BlockPos(x, y, z), mobId);
        if (mob == null) {
            com.political.expansion2.mobs.ExpansionMobs2.spawnById(level, new BlockPos(x, y, z), mobId);
        }
    }

    private static void spawnVillager(ServerLevel level, int x, int y, int z) {
        Mob v = EntityTypes.VILLAGER.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (v == null) return;
        v.setPos(x + 0.5, y, z + 0.5);
        v.setPersistenceRequired();
        level.addFreshEntity(v);
    }

    private static void spawnTrader(ServerLevel level, int x, int y, int z) {
        Mob t = EntityTypes.WANDERING_TRADER.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (t == null) { spawnVillager(level, x, y, z); return; }
        t.setPos(x + 0.5, y, z + 0.5);
        t.setPersistenceRequired();
        level.addFreshEntity(t);
    }

    private static void spawnCursedSpirit(ServerLevel level, int x, int y, int z, int grade) {
        try {
            com.political.expansion2.curses.CurseSpirits2.spawnAt(level, new BlockPos(x, y, z), grade);
        } catch (Throwable t) {
            // Curse system optional at this site; ambient flavour only.
        }
    }

    private static ItemStack modItem(String id) {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getValue(Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}
