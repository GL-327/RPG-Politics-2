package com.political.world.dungeons;

import com.political.RpgPoliticsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Random;

/** Applies post-generation hooks: chest loot, spawners, traps, and mob summons. */
public final class DungeonLoot {

    private static final Random RNG = new Random();

    private DungeonLoot() {}

    public static void applyPostOps(ServerLevel level, DungeonPlan plan) {
        for (DungeonPlan.PostOp op : plan.postOps) {
            switch (op.kind) {
                case CHEST_COMMON -> fillChest(level, op.x, op.y, op.z, DungeonTier.COMMON, plan.site.type);
                case CHEST_UNCOMMON -> fillChest(level, op.x, op.y, op.z, DungeonTier.UNCOMMON, plan.site.type);
                case CHEST_RARE -> fillChest(level, op.x, op.y, op.z, DungeonTier.RARE, plan.site.type);
                case CHEST_EPIC -> fillChest(level, op.x, op.y, op.z, DungeonTier.EPIC, plan.site.type);
                case CHEST_BOSS -> fillBossChest(level, op.x, op.y, op.z, plan.site.type);
                case SPAWNER -> configureSpawner(level, op.x, op.y, op.z, op.mobId);
                case ARROW_TRAP -> fillDispenser(level, op.x, op.y, op.z);
                case MOB, BOSS -> spawnMob(level, op.x, op.y, op.z, op.mobId);
                case SOUL_FIRE_TRAP, WATER_FILL -> { /* blocks already placed */ }
            }
        }
    }

    private static void fillChest(ServerLevel level, int x, int y, int z, DungeonTier tier, DungeonType type) {
        if (tryLootTable(level, x, y, z, tier.lootId)) return;
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof ChestBlockEntity chest)) return;
        chest.clearContent();
        addStack(chest, modItem(type.lootA), 1 + RNG.nextInt(2));
        if (RNG.nextFloat() < 0.6f) addStack(chest, modItem(type.lootB), 1);
        if (tier.level >= DungeonTier.RARE.level && RNG.nextFloat() < 0.35f) {
            addStack(chest, modItem(type.lootC), 1);
        }
        if (tier.level >= DungeonTier.UNCOMMON.level && RNG.nextFloat() < 0.5f) {
            addStack(chest, modItem("acc_minor_healing_potion"), 1 + RNG.nextInt(2));
        }
        if (type == DungeonType.BANDIT_HIDEOUT && RNG.nextFloat() < 0.7f) {
            addStack(chest, modItem("coin_pouch"), 1);
        }
    }

    private static void fillBossChest(ServerLevel level, int x, int y, int z, DungeonType type) {
        if (tryLootTable(level, x, y, z, "boss")) return;
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof ChestBlockEntity chest)) return;
        chest.clearContent();
        addStack(chest, modItem(type.lootC), 1);
        addStack(chest, modItem(type.lootB), 1 + RNG.nextInt(2));
        addStack(chest, modItem("reforge_stone"), 1);
        if (RNG.nextFloat() < 0.5f) addStack(chest, modItem("bounty_seal"), 1);
        if (type.tier == DungeonTier.EPIC) addStack(chest, modItem("treasury_note"), 1);
    }

    private static boolean tryLootTable(ServerLevel level, int x, int y, int z, String tierId) {
        try {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                    Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, "chests/dungeon/" + tierId));
            LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
            BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
            if (!(be instanceof ChestBlockEntity chest)) return false;
            chest.clearContent();
            net.minecraft.world.level.storage.loot.LootParams params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                    .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
            table.fill(chest, params, level.getRandom().nextLong());
            return true;
        } catch (Exception e) {
            return false;
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
        spawner.setEntityId(net.minecraft.world.entity.EntityTypes.ZOMBIE, level.getRandom());
    }

    private static void fillDispenser(ServerLevel level, int x, int y, int z) {
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof DispenserBlockEntity disp)) return;
        disp.setItem(0, new ItemStack(net.minecraft.world.item.Items.ARROW, 16));
        disp.setItem(1, new ItemStack(net.minecraft.world.item.Items.ARROW, 16));
    }

    private static void spawnMob(ServerLevel level, int x, int y, int z, String mobId) {
        if (mobId == null) return;
        var mob = com.political.expansion.mobs.ExpansionMobs.spawnById(level, new BlockPos(x, y, z), mobId);
        if (mob == null) {
            com.political.expansion2.mobs.ExpansionMobs2.spawnById(level, new BlockPos(x, y, z), mobId);
        }
    }

    private static void addStack(ChestBlockEntity chest, ItemStack stack, int count) {
        if (stack.isEmpty()) return;
        stack.setCount(count);
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).isEmpty()) {
                chest.setItem(i, stack);
                return;
            }
        }
    }

    private static ItemStack modItem(String id) {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getValue(Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}
