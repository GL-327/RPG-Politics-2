package com.political.curse;

import com.political.combat.StatManager;
import com.political.politics.DataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Cursed objects: items steeped in cursed energy that a sorcerer can consume to
 * replenish their reserves. Some items are inherently cursed (a dragon egg always
 * is); others may very rarely turn up cursed in loot, and ordinary items
 * left where great slaughter occurred can slowly become cursed and draw curses in.
 */
public final class CursedObjects {

    public static final String CURSED_KEY = "cursed_object";
    public static final String AMOUNT_KEY = "cursed_energy";

    private static final Random RNG = new Random();

    /** Items that "might" become cursed objects, mapped to the cursed energy they carry. */
    private static final Map<Item, Integer> ELIGIBLE = new HashMap<>();
    static {
        ELIGIBLE.put(Items.BONE, 18);
        ELIGIBLE.put(Items.SKELETON_SKULL, 25);
        ELIGIBLE.put(Items.WITHER_SKELETON_SKULL, 45);
        ELIGIBLE.put(Items.ZOMBIE_HEAD, 25);
        ELIGIBLE.put(Items.CREEPER_HEAD, 25);
        ELIGIBLE.put(Items.PIGLIN_HEAD, 25);
        ELIGIBLE.put(Items.WITHER_ROSE, 30);
        ELIGIBLE.put(Items.ECHO_SHARD, 35);
        ELIGIBLE.put(Items.TOTEM_OF_UNDYING, 50);
        ELIGIBLE.put(Items.NETHER_STAR, 70);
        ELIGIBLE.put(Items.BELL, 40);
    }

    /** Per-chunk running death tally (runtime only) for death-density cursing. */
    private static final Map<Long, Integer> DEATH_TALLY = new HashMap<>();

    private CursedObjects() {}

    public static void register() {
        // Sneak + right-click a cursed object to consume it for cursed energy.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !sp.isShiftKeyDown())
                return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            if (!isCursed(stack)) return InteractionResult.PASS;
            return consume(sp, stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        });

        // Very rarely curse eligible items that drop from chest loot tables.
        LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
            var keyOpt = entry.unwrapKey();
            if (keyOpt.isEmpty() || !keyOpt.get().identifier().getPath().contains("chest")) return;
            double chance = DataManager.data().cursedObjectLootChance;
            for (ItemStack stack : drops) {
                if (!isCursed(stack) && ELIGIBLE.containsKey(stack.getItem()) && RNG.nextDouble() < chance) {
                    makeCursed(stack, ELIGIBLE.get(stack.getItem()));
                }
            }
        });

        // Death-density cursing: where many die, nearby items can turn cursed.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity.level().isClientSide()) return;
            if (!(entity.level() instanceof ServerLevel level)) return;
            long chunkKey = chunkKey(entity.blockPosition().getX() >> 4, entity.blockPosition().getZ() >> 4);
            int tally = DEATH_TALLY.merge(chunkKey, 1, Integer::sum);
            if (tally < DataManager.data().deathCurseThreshold) return;
            if (RNG.nextDouble() >= DataManager.data().deathCurseChance) return;
            AABB box = new AABB(entity.blockPosition()).inflate(12);
            for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, box)) {
                ItemStack st = ie.getItem();
                if (!isCursed(st) && ELIGIBLE.containsKey(st.getItem())) {
                    makeCursed(st, ELIGIBLE.get(st.getItem()) + 10);
                    ie.setItem(st);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                            ie.getX(), ie.getY() + 0.3, ie.getZ(), 20, 0.2, 0.2, 0.2, 0.02);
                    DEATH_TALLY.put(chunkKey, 0);
                    break;
                }
            }
        });
    }

    public static boolean isCursed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(Items.DRAGON_EGG)) return true; // a dragon egg is always a cursed object
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(CURSED_KEY, false);
    }

    public static int cursedAmount(ItemStack stack) {
        if (stack.is(Items.DRAGON_EGG)) return 100;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        return data.copyTag().getIntOr(AMOUNT_KEY, 20);
    }

    /** Marks a stack as a cursed object carrying {@code amount} cursed energy. */
    public static void makeCursed(ItemStack stack, int amount) {
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        tag.putBoolean(CURSED_KEY, true);
        tag.putInt(AMOUNT_KEY, amount);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE);

        List<Component> lore = List.of(
                Component.literal("Cursed Object").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                Component.literal("Sneak + use to absorb " + amount + " cursed energy.").withStyle(ChatFormatting.GRAY),
                Component.literal("Draws curses to its bearer.").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
    }

    /** Consume a cursed object for cursed energy; false if the player can't hold any. */
    public static boolean consume(ServerPlayer player, ItemStack stack) {
        if (StatManager.getMaxCursedEnergy(player) <= 0) {
            player.sendSystemMessage(Component.literal("You have no cursed energy to hold \u2014 the object is inert in your hands.")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }
        int amount = cursedAmount(stack);
        int gained = (int) StatManager.addCursedEnergy(player, amount);
        stack.shrink(1);
        ((ServerLevel) player.level()).playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.6f);
        player.sendSystemMessage(Component.literal("You absorb the cursed object. +" + gained + " Cursed Energy.")
                .withStyle(ChatFormatting.DARK_PURPLE));
        return true;
    }

    /** True if the player is carrying any cursed object (draws curses). */
    public static boolean carriesCursedObject(ServerPlayer player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (isCursed(inv.getItem(i))) return true;
        }
        return false;
    }

    /** Items that can become cursed objects, mapped to cursed energy they carry. */
    public static Map<Item, Integer> eligible() {
        return Map.copyOf(ELIGIBLE);
    }

    private static long chunkKey(int cx, int cz) {
        return (((long) cx) << 32) ^ (cz & 0xffffffffL);
    }
}
