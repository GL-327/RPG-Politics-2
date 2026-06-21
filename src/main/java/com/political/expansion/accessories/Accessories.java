package com.political.expansion.accessories;

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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Expansion content: a large catalogue of Skyblock-style accessories (talismans, rings,
 * artifacts, relics) and consumables (potions, elixirs, foods, teleport scrolls).
 *
 * <p>Accessories grant their bonuses passively <b>while simply held in the inventory</b>
 * (Prominence-II / Skyblock style). A fully self-contained inventory-tick handler scans
 * each online player's inventory once per second and applies the aggregated bonuses via
 * vanilla attribute modifiers (using this package's own modifier IDs, so they never clash
 * with {@code StatManager}'s), vanilla {@link MobEffects}, and mana / cursed-energy trickle
 * through the existing public {@link StatManager} API. Nothing in the shared stat pipeline
 * is modified.
 */
public final class Accessories {

    public static final String MOD_ID = "politicalserver";

    private static final List<AccessoryDef> ACCESSORY_DEFS = new ArrayList<>();
    private static final List<ConsumableDef> CONSUMABLE_DEFS = new ArrayList<>();

    private static final Map<String, Item> ALL = new LinkedHashMap<>();
    private static final Map<Item, AccessoryDef> ACCESSORY_BY_ITEM = new HashMap<>();
    private static final Map<Item, ConsumableDef> CONSUMABLE_BY_ITEM = new HashMap<>();

    // Our own attribute-modifier IDs (separate namespace from StatManager's rpg_* IDs).
    private static final Identifier ID_HEALTH = id("acc_health");
    private static final Identifier ID_ARMOR = id("acc_armor");
    private static final Identifier ID_TOUGHNESS = id("acc_toughness");
    private static final Identifier ID_KNOCKBACK = id("acc_knockback");
    private static final Identifier ID_SPEED = id("acc_speed");
    private static final Identifier ID_ATTACK_SPEED = id("acc_attack_speed");
    private static final Identifier ID_LUCK = id("acc_luck");

    private static final String DEC_FLAG = "acc_dec";
    private static final int EFFECT_DURATION_TICKS = 60; // refreshed every second, decays shortly after removal

    private static int tickCounter = 0;

    private Accessories() {}

    /** Exposed for the integration agent: every registered item (accessories + consumables). */
    public static List<Item> items() {
        return new ArrayList<>(ALL.values());
    }

    public static List<AccessoryDef> accessoryDefs() {
        return List.copyOf(ACCESSORY_DEFS);
    }

    public static List<ConsumableDef> consumableDefs() {
        return List.copyOf(CONSUMABLE_DEFS);
    }

    public static void register() {
        buildAccessories();
        buildConsumables();

        for (AccessoryDef def : ACCESSORY_DEFS) {
            Item item = reg(def.id, 1);
            ACCESSORY_BY_ITEM.put(item, def);
        }
        for (ConsumableDef def : CONSUMABLE_DEFS) {
            Item item = reg(def.id, def.stackSize);
            CONSUMABLE_BY_ITEM.put(item, def);
        }

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            ConsumableDef def = CONSUMABLE_BY_ITEM.get(stack.getItem());
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

    /** Skyblock strength from inventory accessories (does not touch vanilla attack damage). */
    public static double strengthBonus(ServerPlayer player) {
        double total = 0;
        Set<AccessoryDef> counted = new HashSet<>();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            AccessoryDef acc = ACCESSORY_BY_ITEM.get(inv.getItem(i).getItem());
            if (acc != null && counted.add(acc)) total += acc.bonus.strength;
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Inventory-tick: scan, decorate, apply bonuses
    // ------------------------------------------------------------------

    private static void applyToPlayer(ServerPlayer player) {
        AccessoryDef.Bonus total = new AccessoryDef.Bonus();
        Map<Holder<MobEffect>, Integer> effects = new HashMap<>();
        Set<AccessoryDef> counted = new HashSet<>();

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();

            AccessoryDef acc = ACCESSORY_BY_ITEM.get(item);
            if (acc != null) {
                decorateAccessory(stack, acc);
                if (counted.add(acc)) accumulate(total, effects, acc.bonus);
                continue;
            }
            ConsumableDef con = CONSUMABLE_BY_ITEM.get(item);
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

    private static void accumulate(AccessoryDef.Bonus total, Map<Holder<MobEffect>, Integer> effects, AccessoryDef.Bonus b) {
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
        for (AccessoryDef.EffectSpec e : b.effects) {
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

    // ------------------------------------------------------------------
    // Decoration (name + Skyblock lore), stamped once per stack
    // ------------------------------------------------------------------

    private static void decorateAccessory(ItemStack stack, AccessoryDef def) {
        if (isDecorated(stack)) return;
        stack.set(DataComponents.CUSTOM_NAME, AccessoryTooltip.name(def));
        stack.set(DataComponents.LORE, new ItemLore(AccessoryTooltip.lore(def)));
        markDecorated(stack);
    }

    private static void decorateConsumable(ItemStack stack, ConsumableDef def) {
        if (isDecorated(stack)) return;
        stack.set(DataComponents.CUSTOM_NAME, AccessoryTooltip.name(def));
        stack.set(DataComponents.LORE, new ItemLore(AccessoryTooltip.lore(def)));
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

    /** Builds a decorated display stack for creative tabs / giving. */
    public static ItemStack display(Item item) {
        ItemStack stack = new ItemStack(item);
        AccessoryDef acc = ACCESSORY_BY_ITEM.get(item);
        if (acc != null) { decorateAccessory(stack, acc); return stack; }
        ConsumableDef con = CONSUMABLE_BY_ITEM.get(item);
        if (con != null) decorateConsumable(stack, con);
        return stack;
    }

    // ------------------------------------------------------------------
    // Consumable helpers
    // ------------------------------------------------------------------

    private static void buff(ServerPlayer sp, Holder<MobEffect> effect, int seconds, int amplifier) {
        sp.addEffect(new MobEffectInstance(effect, seconds * 20, amplifier, false, true, true));
    }

    private static boolean healFraction(ServerPlayer sp, double frac) {
        sp.heal((float) (sp.getMaxHealth() * frac));
        return true;
    }

    private static boolean restoreMana(ServerPlayer sp, double frac) {
        StatManager.addMana(sp, StatManager.getMaxMana(sp) * frac);
        return true;
    }

    private static boolean restoreCursed(ServerPlayer sp, double frac) {
        if (StatManager.getMaxCursedEnergy(sp) <= 0) {
            sp.sendSystemMessage(Component.literal("You have no cursed energy to restore.").withStyle(ChatFormatting.GRAY));
            return false;
        }
        StatManager.addCursedEnergy(sp, StatManager.getMaxCursedEnergy(sp) * frac);
        return true;
    }

    private static boolean blinkForward(ServerPlayer sp, double distance) {
        Vec3 look = sp.getLookAngle();
        double x = sp.getX() + look.x * distance;
        double z = sp.getZ() + look.z * distance;
        sp.teleportTo(x, sp.getY(), z);
        return true;
    }

    private static boolean ascendToSurface(ServerPlayer sp) {
        ServerLevel level = sp.level();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, sp.getBlockX(), sp.getBlockZ());
        sp.teleportTo(sp.getX(), y, sp.getZ());
        sp.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true, true));
        return true;
    }

    private static boolean recallToSpawn(ServerPlayer sp) {
        ServerLevel overworld = sp.level().getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return false;
        var respawn = overworld.getRespawnData();
        var pos = respawn.pos();
        sp.teleportTo(overworld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.<Relative>of(), sp.getYRot(), sp.getXRot(), true);
        return true;
    }

    // ------------------------------------------------------------------
    // Catalogue definitions
    // ------------------------------------------------------------------

    private static void acc(String id, String name, AccessoryDef.Type type, Rarity rarity, String flavor, AccessoryDef.Bonus bonus) {
        ACCESSORY_DEFS.add(new AccessoryDef(id, name, type, rarity, flavor, bonus));
    }

    private static void buildAccessories() {
        acc("acc_warding_talisman", "Warding Talisman", AccessoryDef.Type.TALISMAN, Rarity.UNCOMMON,
                "A faint ward turns aside the worst of blows.",
                AccessoryDef.bonus().health(20).defense(15));
        acc("acc_vigor_ring", "Ring of Vigor", AccessoryDef.Type.RING, Rarity.UNCOMMON,
                "Hardy life flows through the band.",
                AccessoryDef.bonus().health(40));
        acc("acc_berserker_band", "Berserker's Band", AccessoryDef.Type.BAND, Rarity.RARE,
                "It hungers for the thrill of the fray.",
                AccessoryDef.bonus().strength(25).ferocity(8));
        acc("acc_swiftness_charm", "Swiftness Charm", AccessoryDef.Type.CHARM, Rarity.UNCOMMON,
                "Your steps grow light and quick.",
                AccessoryDef.bonus().speed(30).effect(MobEffects.SPEED, 0, "Speed I"));
        acc("acc_scholar_amulet", "Scholar's Amulet", AccessoryDef.Type.AMULET, Rarity.RARE,
                "Arcane study replenishes the mind.",
                AccessoryDef.bonus().manaRegen(4));
        acc("acc_cursed_seal", "Cursed Seal", AccessoryDef.Type.TALISMAN, Rarity.EPIC,
                "A sealed sliver of cursed energy seeps back into you.",
                AccessoryDef.bonus().cursedRegen(3));
        acc("acc_titan_heart", "Titan Heart", AccessoryDef.Type.ARTIFACT, Rarity.EPIC,
                "The petrified heart of a fallen titan.",
                AccessoryDef.bonus().health(80).defense(30).toughness(4));
        acc("acc_assassin_ring", "Assassin's Ring", AccessoryDef.Type.RING, Rarity.RARE,
                "Strike from the shadows, swift and sure.",
                AccessoryDef.bonus().critChance(15).critDamage(40).speed(10));
        acc("acc_bruiser_totem", "Bruiser Totem", AccessoryDef.Type.TOTEM, Rarity.RARE,
                "Stand your ground; let them break upon you.",
                AccessoryDef.bonus().health(50).strength(15).knockback(0.4));
        acc("acc_phoenix_charm", "Phoenix Charm", AccessoryDef.Type.CHARM, Rarity.EPIC,
                "Wreathed in embers that never burn its bearer.",
                AccessoryDef.bonus().health(30).effect(MobEffects.FIRE_RESISTANCE, 0, "Fire Resistance"));
        acc("acc_aqua_pendant", "Aqua Pendant", AccessoryDef.Type.AMULET, Rarity.UNCOMMON,
                "The sea welcomes you as its own.",
                AccessoryDef.bonus().effect(MobEffects.WATER_BREATHING, 0, "Water Breathing")
                        .effect(MobEffects.DOLPHINS_GRACE, 0, "Dolphin's Grace"));
        acc("acc_owl_talisman", "Owl Talisman", AccessoryDef.Type.TALISMAN, Rarity.UNCOMMON,
                "See clearly through the darkest night.",
                AccessoryDef.bonus().effect(MobEffects.NIGHT_VISION, 0, "Night Vision"));
        acc("acc_feather_charm", "Feather Charm", AccessoryDef.Type.CHARM, Rarity.RARE,
                "Light as a feather, sure of foot.",
                AccessoryDef.bonus().speed(10).effect(MobEffects.SLOW_FALLING, 0, "Slow Falling")
                        .effect(MobEffects.JUMP_BOOST, 1, "Jump Boost II"));
        acc("acc_miners_band", "Miner's Band", AccessoryDef.Type.BAND, Rarity.UNCOMMON,
                "The rhythm of the pick quickens.",
                AccessoryDef.bonus().luck(2).effect(MobEffects.HASTE, 1, "Haste II"));
        acc("acc_lucky_clover", "Lucky Clover", AccessoryDef.Type.CHARM, Rarity.RARE,
                "Fortune favours the prepared.",
                AccessoryDef.bonus().luck(5).effect(MobEffects.LUCK, 1, "Luck II"));
        acc("acc_guardian_artifact", "Guardian Artifact", AccessoryDef.Type.ARTIFACT, Rarity.LEGENDARY,
                "An ancient bulwark answers your need.",
                AccessoryDef.bonus().health(60).defense(60).effect(MobEffects.RESISTANCE, 0, "Resistance I"));
        acc("acc_warlords_signet", "Warlord's Signet", AccessoryDef.Type.RING, Rarity.LEGENDARY,
                "The seal of a conqueror of nations.",
                AccessoryDef.bonus().strength(40).critDamage(60).ferocity(12));
        acc("acc_arcane_orb", "Arcane Orb", AccessoryDef.Type.ARTIFACT, Rarity.EPIC,
                "A wellspring of pure mana orbits your hand.",
                AccessoryDef.bonus().manaRegen(10));
        acc("acc_vampiric_charm", "Vampiric Charm", AccessoryDef.Type.CHARM, Rarity.EPIC,
                "It thirsts, and shares its vitality with you.",
                AccessoryDef.bonus().strength(20).effect(MobEffects.REGENERATION, 0, "Regeneration I"));
        acc("acc_golem_core", "Golem Core", AccessoryDef.Type.ARTIFACT, Rarity.LEGENDARY,
                "The beating stone heart of a war golem.",
                AccessoryDef.bonus().health(120).defense(50).knockback(0.6).toughness(6));
        acc("acc_windrunner_anklet", "Windrunner Anklet", AccessoryDef.Type.RING, Rarity.RARE,
                "Run on the wind itself.",
                AccessoryDef.bonus().speed(40).attackSpeed(20).effect(MobEffects.JUMP_BOOST, 0, "Jump Boost I"));
        acc("acc_dragon_scale", "Dragon Scale", AccessoryDef.Type.TALISMAN, Rarity.LEGENDARY,
                "A single scale, hard as a mountain and cool to fire.",
                AccessoryDef.bonus().health(50).defense(40).effect(MobEffects.FIRE_RESISTANCE, 0, "Fire Resistance"));
        acc("acc_soul_lantern_charm", "Soul Lantern Charm", AccessoryDef.Type.CHARM, Rarity.EPIC,
                "A captured soul lights your way and feeds your curse.",
                AccessoryDef.bonus().cursedRegen(2).effect(MobEffects.NIGHT_VISION, 0, "Night Vision"));
        acc("acc_executioners_emblem", "Executioner's Emblem", AccessoryDef.Type.TALISMAN, Rarity.EPIC,
                "Every strike lands like a falling axe.",
                AccessoryDef.bonus().strength(20).critDamage(80));
        acc("acc_sentinel_aegis", "Sentinel Aegis", AccessoryDef.Type.ARTIFACT, Rarity.MYTHIC,
                "The undying shield of the last sentinel.",
                AccessoryDef.bonus().health(150).defense(80).toughness(8).effect(MobEffects.RESISTANCE, 0, "Resistance I"));
        acc("acc_godslayer_relic", "Godslayer Relic", AccessoryDef.Type.RELIC, Rarity.MYTHIC,
                "Forged to kill that which cannot die.",
                AccessoryDef.bonus().strength(60).critChance(20).critDamage(100).ferocity(20)
                        .effect(MobEffects.STRENGTH, 0, "Strength I"));
    }

    private static void con(String id, String name, ConsumableDef.Type type, Rarity rarity, String flavor,
                            String successMsg, ChatFormatting color, int stackSize, ConsumableDef.Action action) {
        CONSUMABLE_DEFS.add(new ConsumableDef(id, name, type, rarity, flavor, successMsg, color, stackSize, action));
    }

    private static void buildConsumables() {
        con("acc_minor_healing_potion", "Minor Healing Potion", ConsumableDef.Type.POTION, Rarity.COMMON,
                "Restores 40% of your max Health.", "Warmth knits your wounds closed.", ChatFormatting.RED, 16,
                sp -> healFraction(sp, 0.40));
        con("acc_greater_healing_potion", "Greater Healing Potion", ConsumableDef.Type.POTION, Rarity.UNCOMMON,
                "Restores your Health to full.", "You are made whole again.", ChatFormatting.RED, 16,
                sp -> healFraction(sp, 1.0));
        con("acc_mana_potion", "Mana Potion", ConsumableDef.Type.POTION, Rarity.COMMON,
                "Restores 40% of your max Mana.", "Mana trickles back into you.", ChatFormatting.AQUA, 16,
                sp -> restoreMana(sp, 0.40));
        con("acc_greater_mana_potion", "Greater Mana Potion", ConsumableDef.Type.POTION, Rarity.UNCOMMON,
                "Restores your Mana to full.", "Your mind floods with mana.", ChatFormatting.AQUA, 16,
                sp -> restoreMana(sp, 1.0));
        con("acc_cursed_energy_vial", "Cursed Energy Vial", ConsumableDef.Type.POTION, Rarity.RARE,
                "Restores 60% of your max Cursed Energy.", "Cursed energy boils up within you.", ChatFormatting.DARK_PURPLE, 16,
                sp -> restoreCursed(sp, 0.60));
        con("acc_elixir_of_strength", "Elixir of Strength", ConsumableDef.Type.ELIXIR, Rarity.RARE,
                "Strength III for 90 seconds.", "Power surges into your limbs.", ChatFormatting.RED, 16,
                sp -> { buff(sp, MobEffects.STRENGTH, 90, 2); return true; });
        con("acc_elixir_of_swiftness", "Elixir of Swiftness", ConsumableDef.Type.ELIXIR, Rarity.UNCOMMON,
                "Speed III for 120 seconds.", "The world slows around you.", ChatFormatting.WHITE, 16,
                sp -> { buff(sp, MobEffects.SPEED, 120, 2); return true; });
        con("acc_elixir_of_iron_skin", "Elixir of Iron Skin", ConsumableDef.Type.ELIXIR, Rarity.RARE,
                "Resistance II for 60 seconds.", "Your skin hardens like iron.", ChatFormatting.GREEN, 16,
                sp -> { buff(sp, MobEffects.RESISTANCE, 60, 1); return true; });
        con("acc_elixir_of_haste", "Elixir of Haste", ConsumableDef.Type.ELIXIR, Rarity.UNCOMMON,
                "Haste III for 120 seconds.", "Your hands move in a blur.", ChatFormatting.GOLD, 16,
                sp -> { buff(sp, MobEffects.HASTE, 120, 2); return true; });
        con("acc_elixir_of_the_phoenix", "Elixir of the Phoenix", ConsumableDef.Type.ELIXIR, Rarity.EPIC,
                "Fire Resistance + Regeneration II for 60 seconds.", "Phoenix fire wreathes you.", ChatFormatting.GOLD, 16,
                sp -> { buff(sp, MobEffects.FIRE_RESISTANCE, 60, 0); buff(sp, MobEffects.REGENERATION, 60, 1); return true; });
        con("acc_berserk_brew", "Berserk Brew", ConsumableDef.Type.ELIXIR, Rarity.EPIC,
                "Strength IV + Speed II for 90 seconds.", "A red haze descends. KILL.", ChatFormatting.DARK_RED, 16,
                sp -> { buff(sp, MobEffects.STRENGTH, 90, 3); buff(sp, MobEffects.SPEED, 90, 1); return true; });
        con("acc_invisibility_draught", "Invisibility Draught", ConsumableDef.Type.POTION, Rarity.RARE,
                "Invisibility for 45 seconds.", "You fade from sight.", ChatFormatting.GRAY, 16,
                sp -> { buff(sp, MobEffects.INVISIBILITY, 45, 0); return true; });
        con("acc_night_owl_tonic", "Night Owl Tonic", ConsumableDef.Type.POTION, Rarity.COMMON,
                "Night Vision for 5 minutes.", "Your eyes drink in the dark.", ChatFormatting.BLUE, 16,
                sp -> { buff(sp, MobEffects.NIGHT_VISION, 300, 0); return true; });
        con("acc_gills_brew", "Gills Brew", ConsumableDef.Type.POTION, Rarity.UNCOMMON,
                "Water Breathing + Dolphin's Grace for 3 minutes.", "You breathe the deep.", ChatFormatting.AQUA, 16,
                sp -> { buff(sp, MobEffects.WATER_BREATHING, 180, 0); buff(sp, MobEffects.DOLPHINS_GRACE, 180, 0); return true; });
        con("acc_titan_tonic", "Titan Tonic", ConsumableDef.Type.ELIXIR, Rarity.EPIC,
                "Health Boost II + Absorption II for 2 minutes.", "You swell with titanic vitality.", ChatFormatting.RED, 16,
                sp -> { buff(sp, MobEffects.HEALTH_BOOST, 120, 1); buff(sp, MobEffects.ABSORPTION, 120, 1); return true; });
        con("acc_hearty_stew", "Hearty Stew", ConsumableDef.Type.FOOD, Rarity.COMMON,
                "Sates hunger and heals a little.", "The hot stew restores you.", ChatFormatting.GOLD, 16,
                sp -> { buff(sp, MobEffects.SATURATION, 1, 2); healFraction(sp, 0.15); return true; });
        con("acc_golden_feast", "Golden Feast", ConsumableDef.Type.FOOD, Rarity.RARE,
                "A lavish feast: Saturation, Regeneration & Absorption.", "You feast like royalty.", ChatFormatting.GOLD, 16,
                sp -> { buff(sp, MobEffects.SATURATION, 1, 4); buff(sp, MobEffects.REGENERATION, 12, 1);
                        buff(sp, MobEffects.ABSORPTION, 120, 1); return true; });
        con("acc_spirit_bread", "Spirit Bread", ConsumableDef.Type.FOOD, Rarity.UNCOMMON,
                "Sates hunger and restores some Mana.", "The blessed bread feeds body and mind.", ChatFormatting.AQUA, 16,
                sp -> { buff(sp, MobEffects.SATURATION, 1, 2); restoreMana(sp, 0.30); return true; });
        con("acc_cursed_jerky", "Cursed Jerky", ConsumableDef.Type.FOOD, Rarity.RARE,
                "Sates hunger and restores some Cursed Energy.", "Bitter, but it stokes your curse.", ChatFormatting.DARK_PURPLE, 16,
                sp -> { buff(sp, MobEffects.SATURATION, 1, 2); restoreCursed(sp, 0.30); return true; });
        con("acc_scroll_of_recall", "Scroll of Recall", ConsumableDef.Type.SCROLL, Rarity.RARE,
                "Teleports you to the world spawn.", "The runes pull you home.", ChatFormatting.LIGHT_PURPLE, 16,
                Accessories::recallToSpawn);
        con("acc_scroll_of_blink", "Scroll of Blink", ConsumableDef.Type.SCROLL, Rarity.UNCOMMON,
                "Blink 8 blocks in the direction you face.", "Space folds and you step through.", ChatFormatting.LIGHT_PURPLE, 16,
                sp -> blinkForward(sp, 8.0));
        con("acc_scroll_of_ascension", "Scroll of Ascension", ConsumableDef.Type.SCROLL, Rarity.RARE,
                "Teleports you up to the surface.", "You rise toward the sky.", ChatFormatting.LIGHT_PURPLE, 16,
                Accessories::ascendToSurface);
        con("acc_scroll_of_warding", "Scroll of Warding", ConsumableDef.Type.SCROLL, Rarity.EPIC,
                "Resistance II + Absorption II for 60 seconds.", "A protective ward flares around you.", ChatFormatting.GREEN, 16,
                sp -> { buff(sp, MobEffects.RESISTANCE, 60, 1); buff(sp, MobEffects.ABSORPTION, 60, 1); return true; });
        con("acc_scroll_of_haste", "Scroll of Haste", ConsumableDef.Type.SCROLL, Rarity.UNCOMMON,
                "Speed II + Haste II for 60 seconds.", "Quickening runes race across your skin.", ChatFormatting.YELLOW, 16,
                sp -> { buff(sp, MobEffects.SPEED, 60, 1); buff(sp, MobEffects.HASTE, 60, 1); return true; });
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

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
