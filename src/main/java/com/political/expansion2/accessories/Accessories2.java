package com.political.expansion2.accessories;

import com.political.combat.StatManager;
import com.political.items.Rarity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Expansion2 accessories: acc2_* talismans, rings, artifacts, charms, relics, runes,
 * badges, and consumables (potions, scrolls, bombs, foods-on-use).
 */
public final class Accessories2 {

    public static final String MOD_ID = "politicalserver";

    private static final List<AccessoryDef2> ACCESSORY_DEFS = new ArrayList<>();
    private static final List<ConsumableDef2> CONSUMABLE_DEFS = new ArrayList<>();

    private static final Map<String, Item> ALL = new LinkedHashMap<>();
    private static final Map<Item, AccessoryDef2> ACCESSORY_BY_ITEM = new HashMap<>();
    private static final Map<Item, ConsumableDef2> CONSUMABLE_BY_ITEM = new HashMap<>();

    private static final Identifier ID_HEALTH = id("acc2_health");
    private static final Identifier ID_ARMOR = id("acc2_armor");
    private static final Identifier ID_TOUGHNESS = id("acc2_toughness");
    private static final Identifier ID_KNOCKBACK = id("acc2_knockback");
    private static final Identifier ID_SPEED = id("acc2_speed");
    private static final Identifier ID_ATTACK_SPEED = id("acc2_attack_speed");
    private static final Identifier ID_LUCK = id("acc2_luck");

    private static final String DEC_FLAG = "acc2_dec";
    private static final int EFFECT_DURATION_TICKS = 60;

    private static int tickCounter = 0;

    private Accessories2() {}

    public static List<Item> items() {
        return new ArrayList<>(ALL.values());
    }

    public static List<AccessoryDef2> accessoryDefs() {
        return List.copyOf(ACCESSORY_DEFS);
    }

    public static List<ConsumableDef2> consumableDefs() {
        return List.copyOf(CONSUMABLE_DEFS);
    }

    public static void register() {
        Accessories2Catalog.buildAccessories(ACCESSORY_DEFS);
        Accessories2Catalog.buildConsumables(CONSUMABLE_DEFS);

        for (AccessoryDef2 def : ACCESSORY_DEFS) {
            Item item = reg(def.id, 1);
            ACCESSORY_BY_ITEM.put(item, def);
        }
        for (ConsumableDef2 def : CONSUMABLE_DEFS) {
            Item item = reg(def.id, def.stackSize);
            CONSUMABLE_BY_ITEM.put(item, def);
        }

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            ConsumableDef2 def = CONSUMABLE_BY_ITEM.get(stack.getItem());
            if (def == null) return InteractionResult.PASS;
            boolean ok = def.action.apply(sp);
            if (!ok) return InteractionResult.FAIL;
            sp.sendSystemMessage(Component.literal(def.successMsg).withStyle(def.msgColor));
            if (!sp.isCreative()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tickCounter % 20 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                applyToPlayer(player);
            }
        });
    }

    // ------------------------------------------------------------------
    // Package-private helpers used by generated Accessories2Catalog
    // ------------------------------------------------------------------

    static void acc(List<AccessoryDef2> out, String id, String name, AccessoryDef2.Type type,
                      Rarity rarity, String flavor, AccessoryDef2.Bonus bonus) {
        out.add(new AccessoryDef2(id, name, type, rarity, flavor, bonus));
    }

    static void con(List<ConsumableDef2> out, String id, String name, ConsumableDef2.Type type,
                    Rarity rarity, String flavor, String successMsg, ChatFormatting color,
                    int stackSize, ConsumableDef2.Action action) {
        out.add(new ConsumableDef2(id, name, type, rarity, flavor, successMsg, color, stackSize, action));
    }

    static void buff(ServerPlayer sp, Holder<MobEffect> effect, int seconds, int amplifier) {
        sp.addEffect(new MobEffectInstance(effect, seconds * 20, amplifier, false, true, true));
    }

    static boolean healFrac(ServerPlayer sp, double frac) {
        sp.heal((float) (sp.getMaxHealth() * frac));
        return true;
    }

    static boolean restoreMana(ServerPlayer sp, double frac) {
        StatManager.addMana(sp, StatManager.getMaxMana(sp) * frac);
        return true;
    }

    static boolean restoreCursed(ServerPlayer sp, double frac) {
        if (StatManager.getMaxCursedEnergy(sp) <= 0) {
            sp.sendSystemMessage(Component.literal("You have no cursed energy to restore.").withStyle(ChatFormatting.GRAY));
            return false;
        }
        StatManager.addCursedEnergy(sp, StatManager.getMaxCursedEnergy(sp) * frac);
        return true;
    }

    static boolean tripleRestore(ServerPlayer sp) {
        healFrac(sp, 0.50);
        restoreMana(sp, 0.50);
        return restoreCursed(sp, 0.50);
    }

    static boolean clearPoison(ServerPlayer sp) {
        sp.removeEffect(MobEffects.POISON);
        buff(sp, MobEffects.RESISTANCE, 10, 0);
        return true;
    }

    static boolean blinkForward(ServerPlayer sp, double distance) {
        Vec3 look = sp.getLookAngle();
        sp.teleportTo(sp.getX() + look.x * distance, sp.getY(), sp.getZ() + look.z * distance);
        return true;
    }

    static boolean ascendToSurface(ServerPlayer sp) {
        ServerLevel level = sp.level();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, sp.getBlockX(), sp.getBlockZ());
        sp.teleportTo(sp.getX(), y, sp.getZ());
        buff(sp, MobEffects.SLOW_FALLING, 5, 0);
        return true;
    }

    static boolean recallToSpawn(ServerPlayer sp) {
        ServerLevel overworld = sp.level().getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) return false;
        var respawn = overworld.getRespawnData();
        var pos = respawn.pos();
        sp.teleportTo(overworld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                java.util.Set.of(), sp.getYRot(), sp.getXRot(), true);
        return true;
    }

    static boolean foodHeal(ServerPlayer sp, double heal, int satAmp) {
        buff(sp, MobEffects.SATURATION, 1, satAmp);
        return healFrac(sp, heal);
    }

    static boolean foodGolden(ServerPlayer sp) {
        buff(sp, MobEffects.SATURATION, 1, 4);
        buff(sp, MobEffects.REGENERATION, 12, 1);
        buff(sp, MobEffects.ABSORPTION, 120, 1);
        return true;
    }

    static boolean foodMana(ServerPlayer sp, double mana, int satAmp) {
        buff(sp, MobEffects.SATURATION, 1, satAmp);
        return restoreMana(sp, mana);
    }

    static boolean foodCursed(ServerPlayer sp, double frac, int satAmp) {
        buff(sp, MobEffects.SATURATION, 1, satAmp);
        return restoreCursed(sp, frac);
    }

    static boolean foodTriple(ServerPlayer sp) {
        healFrac(sp, 1.0);
        restoreMana(sp, 1.0);
        return restoreCursed(sp, 1.0);
    }

    static boolean foodVoid(ServerPlayer sp) {
        healFrac(sp, 0.75);
        restoreMana(sp, 0.75);
        restoreCursed(sp, 0.75);
        buff(sp, MobEffects.STRENGTH, 60, 1);
        buff(sp, MobEffects.RESISTANCE, 60, 1);
        return true;
    }

    static boolean bombFlash(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0));
        });
        return true;
    }

    static boolean bombFire(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            e.setRemainingFireTicks(80);
            e.hurt(sp.damageSources().onFire(), 4.0f);
        });
        return true;
    }

    static boolean bombFrost(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            e.hurt(sp.damageSources().freeze(), 3.0f);
        });
        return true;
    }

    static boolean bombPoison(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e ->
                e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1)));
        return true;
    }

    static boolean bombSmoke(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e ->
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0)));
        buff(sp, MobEffects.INVISIBILITY, 5, 0);
        return true;
    }

    static boolean bombHoly(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            if (e instanceof net.minecraft.world.entity.monster.Monster) {
                e.hurt(sp.damageSources().magic(), 8.0f);
            }
        });
        return true;
    }

    static boolean bombVoid(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            e.hurt(sp.damageSources().magic(), 10.0f);
            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        });
        return true;
    }

    static boolean bombGravity(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            Vec3 toward = sp.position().subtract(e.position()).normalize().scale(0.8);
            e.setDeltaMovement(toward);
            e.hurtMarked = true;
        });
        return true;
    }

    static boolean bombShock(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            e.hurt(sp.damageSources().lightningBolt(), 5.0f);
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
        });
        return true;
    }

    static boolean bombHeal(ServerPlayer sp, double radius) {
        sp.level().getEntitiesOfClass(LivingEntity.class, aabb(sp, radius), e -> e.isAlive()).forEach(e -> {
            if (e instanceof ServerPlayer p) e.heal((float) (p.getMaxHealth() * 0.15));
            else e.heal(4.0f);
        });
        return true;
    }

    static boolean bombCursed(ServerPlayer sp, double radius) {
        affectInRadius(sp, radius, e -> {
            e.hurt(sp.damageSources().magic(), 6.0f);
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
        });
        restoreCursed(sp, 0.20);
        return true;
    }

    private static void affectInRadius(ServerPlayer sp, double radius, java.util.function.Consumer<LivingEntity> fn) {
        AABB box = aabb(sp, radius);
        for (LivingEntity e : sp.level().getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == sp) continue;
            fn.accept(e);
        }
    }

    private static AABB aabb(ServerPlayer sp, double radius) {
        return new AABB(sp.getX() - radius, sp.getY() - radius, sp.getZ() - radius,
                sp.getX() + radius, sp.getY() + radius, sp.getZ() + radius);
    }

    /** Skyblock strength from inventory accessories (does not touch vanilla attack damage). */
    public static double strengthBonus(ServerPlayer player) {
        double total = 0;
        Set<AccessoryDef2> counted = new HashSet<>();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            AccessoryDef2 acc = ACCESSORY_BY_ITEM.get(inv.getItem(i).getItem());
            if (acc != null && counted.add(acc)) total += acc.bonus.strength;
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Inventory scan + apply
    // ------------------------------------------------------------------

    private static void applyToPlayer(ServerPlayer player) {
        AccessoryDef2.Bonus total = new AccessoryDef2.Bonus();
        Map<Holder<MobEffect>, Integer> effects = new HashMap<>();
        Set<AccessoryDef2> counted = new HashSet<>();

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            AccessoryDef2 acc = ACCESSORY_BY_ITEM.get(stack.getItem());
            if (acc != null) {
                decorateAccessory(stack, acc);
                if (counted.add(acc)) accumulate(total, effects, acc.bonus);
                continue;
            }
            ConsumableDef2 con = CONSUMABLE_BY_ITEM.get(stack.getItem());
            if (con != null) decorateConsumable(stack, con);
        }

        applyAttribute(player, Attributes.MAX_HEALTH, ID_HEALTH, total.health, AttributeModifier.Operation.ADD_VALUE);
        applyAttribute(player, Attributes.ARMOR, ID_ARMOR, total.defense * 0.15, AttributeModifier.Operation.ADD_VALUE);
        applyAttribute(player, Attributes.ARMOR_TOUGHNESS, ID_TOUGHNESS, total.toughness, AttributeModifier.Operation.ADD_VALUE);
        applyAttribute(player, Attributes.KNOCKBACK_RESISTANCE, ID_KNOCKBACK, total.knockbackResist, AttributeModifier.Operation.ADD_VALUE);
        applyAttribute(player, Attributes.MOVEMENT_SPEED, ID_SPEED, total.speed * 0.005, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyAttribute(player, Attributes.ATTACK_SPEED, ID_ATTACK_SPEED, total.attackSpeed * 0.01, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyAttribute(player, Attributes.LUCK, ID_LUCK, total.luck, AttributeModifier.Operation.ADD_VALUE);

        for (Map.Entry<Holder<MobEffect>, Integer> e : effects.entrySet()) {
            player.addEffect(new MobEffectInstance(e.getKey(), EFFECT_DURATION_TICKS, e.getValue(), true, false, true));
        }

        if (total.manaRegen > 0) StatManager.addMana(player, total.manaRegen);
        if (total.cursedRegen > 0 && StatManager.getMaxCursedEnergy(player) > 0) {
            StatManager.addCursedEnergy(player, total.cursedRegen);
        }
    }

    private static void accumulate(AccessoryDef2.Bonus total, Map<Holder<MobEffect>, Integer> effects, AccessoryDef2.Bonus b) {
        total.health += b.health;
        total.defense += b.defense;
        total.strength += b.strength;
        total.toughness += b.toughness;
        total.knockbackResist += b.knockbackResist;
        total.speed += b.speed;
        total.attackSpeed += b.attackSpeed;
        total.luck += b.luck;
        total.manaRegen += b.manaRegen;
        total.cursedRegen += b.cursedRegen;
        for (AccessoryDef2.EffectSpec e : b.effects) {
            effects.merge(e.effect(), e.amplifier(), Math::max);
        }
    }

    private static void applyAttribute(ServerPlayer player, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                       Identifier id, double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, op));
        }
    }

    private static void decorateAccessory(ItemStack stack, AccessoryDef2 def) {
        if (isDecorated(stack)) return;
        stack.set(DataComponents.CUSTOM_NAME, AccessoryTooltip2.name(def));
        stack.set(DataComponents.LORE, new ItemLore(AccessoryTooltip2.lore(def)));
        markDecorated(stack);
    }

    private static void decorateConsumable(ItemStack stack, ConsumableDef2 def) {
        if (isDecorated(stack)) return;
        stack.set(DataComponents.CUSTOM_NAME, AccessoryTooltip2.name(def));
        stack.set(DataComponents.LORE, new ItemLore(AccessoryTooltip2.lore(def)));
        markDecorated(stack);
    }

    private static boolean isDecorated(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(DEC_FLAG, false);
    }

    private static void markDecorated(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        tag.putBoolean(DEC_FLAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static ItemStack display(Item item) {
        ItemStack stack = new ItemStack(item);
        AccessoryDef2 acc = ACCESSORY_BY_ITEM.get(item);
        if (acc != null) { decorateAccessory(stack, acc); return stack; }
        ConsumableDef2 con = CONSUMABLE_BY_ITEM.get(item);
        if (con != null) decorateConsumable(stack, con);
        return stack;
    }

    private static Item reg(String name, int stackSize) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        Item item = new Item(new Item.Properties().stacksTo(stackSize).setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        ALL.put(name, registered);
        return registered;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
