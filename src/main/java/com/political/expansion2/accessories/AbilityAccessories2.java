package com.political.expansion2.accessories;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side engine for the artifacts-inspired {@code acc2_ab_*} ability accessories. Each tick it
 * scans every player's inventory, collects the set of {@link AccessoryAbility2}s they carry, and
 * applies the corresponding effect — flight, item magnetism, fire/knockback immunity, aquatic
 * adaptation, step assist, night sight, grass grazing and featherfall.
 *
 * <p>Self-contained: it does not touch the existing {@link Accessories2} passive-stat engine. Wire
 * it up by calling {@link #register()} from the common initializer (see the integration manifest).
 */
public final class AbilityAccessories2 {

    public static final String MOD_ID = "politicalserver";

    private static final List<AbilityAccessoryDef2> DEFS = new ArrayList<>();
    private static final Map<String, Item> ALL = new LinkedHashMap<>();
    private static final Map<Item, AbilityAccessoryDef2> BY_ITEM = new HashMap<>();

    private static final Identifier ID_KNOCKBACK = id("acc2_ab_knockback");
    private static final Identifier ID_STEP = id("acc2_ab_step");
    private static final Identifier ID_SWIFT = id("acc2_ab_swift");

    private static final String DEC_FLAG = "acc2_ab_dec";

    /** Players we have currently granted accessory-flight to, so we can revoke it cleanly. */
    private static final Set<UUID> grantedFlight = new java.util.HashSet<>();

    private static int tickCounter = 0;

    private AbilityAccessories2() {}

    public static List<Item> items() {
        return new ArrayList<>(ALL.values());
    }

    public static List<AbilityAccessoryDef2> defs() {
        return List.copyOf(DEFS);
    }

    public static void register() {
        AbilityAccessoriesCatalog2.build(DEFS);
        for (AbilityAccessoryDef2 def : DEFS) {
            Item item = reg(def.id);
            BY_ITEM.put(item, def);
        }
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                applyToPlayer(player);
            }
        });
    }

    /** Builds a decorated display stack (name + lore) for creative tabs / catalogs. */
    public static ItemStack display(Item item) {
        ItemStack stack = new ItemStack(item);
        AbilityAccessoryDef2 def = BY_ITEM.get(item);
        if (def != null) decorate(stack, def);
        return stack;
    }

    // ------------------------------------------------------------------
    // Per-player application
    // ------------------------------------------------------------------

    private static void applyToPlayer(ServerPlayer player) {
        EnumSet<AccessoryAbility2> active = EnumSet.noneOf(AccessoryAbility2.class);
        double magnetRadius = 0;
        double stepHeight = 0;

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            AbilityAccessoryDef2 def = BY_ITEM.get(stack.getItem());
            if (def == null) continue;
            decorate(stack, def);
            active.add(def.ability);
            if (def.ability == AccessoryAbility2.MAGNET) magnetRadius = Math.max(magnetRadius, def.magnitude);
            if (def.ability == AccessoryAbility2.STEP_ASSIST) stepHeight = Math.max(stepHeight, def.magnitude);
        }

        applyFlight(player, active.contains(AccessoryAbility2.FLIGHT));
        applyAttribute(player, Attributes.KNOCKBACK_RESISTANCE, ID_KNOCKBACK,
                active.contains(AccessoryAbility2.ANTI_KNOCKBACK) ? 1.0 : 0.0,
                AttributeModifier.Operation.ADD_VALUE);
        applyAttribute(player, Attributes.STEP_HEIGHT, ID_STEP, stepHeight,
                AttributeModifier.Operation.ADD_VALUE);
        applyAttribute(player, Attributes.MOVEMENT_SPEED, ID_SWIFT,
                active.contains(AccessoryAbility2.SWIFTSTRIDE) && player.isSprinting() ? 0.25 : 0.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        if (active.contains(AccessoryAbility2.FIRE_WARD)) {
            if (player.isOnFire()) player.clearFire();
            grant(player, MobEffects.FIRE_RESISTANCE, 40, 0);
        }
        if (active.contains(AccessoryAbility2.AQUATIC)) {
            grant(player, MobEffects.WATER_BREATHING, 40, 0);
            grant(player, MobEffects.DOLPHINS_GRACE, 40, 0);
            grant(player, MobEffects.NIGHT_VISION, 40, 0);
        }
        if (active.contains(AccessoryAbility2.NIGHT_SIGHT)) {
            grant(player, MobEffects.NIGHT_VISION, 300, 0);
        }
        if (active.contains(AccessoryAbility2.FEATHERFALL)) {
            player.resetFallDistance();
            if (player.getDeltaMovement().y < -0.3 && !player.onGround()) {
                grant(player, MobEffects.SLOW_FALLING, 20, 0);
            }
        }
        if (magnetRadius > 0) pullLoot(player, magnetRadius);
        if (active.contains(AccessoryAbility2.GRAZING)) graze(player);
    }

    private static void applyFlight(ServerPlayer player, boolean shouldFly) {
        boolean granted = grantedFlight.contains(player.getUUID());
        if (shouldFly) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            grantedFlight.add(player.getUUID());
        } else if (granted) {
            grantedFlight.remove(player.getUUID());
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    private static void pullLoot(ServerPlayer player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        Vec3 target = player.position().add(0, 0.4, 0);
        ServerLevel level = player.level();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, e -> !e.hasPickUpDelay() || e.tickCount > 10)) {
            Vec3 dir = target.subtract(item.position());
            if (dir.lengthSqr() < 1.2) continue;
            item.setDeltaMovement(dir.normalize().scale(0.45));
        }
        for (net.minecraft.world.entity.ExperienceOrb orb :
                level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, box)) {
            Vec3 dir = target.subtract(orb.position());
            if (dir.lengthSqr() < 1.2) continue;
            orb.setDeltaMovement(dir.normalize().scale(0.45));
        }
    }

    private static void graze(ServerPlayer player) {
        if (tickCounter % 40 != 0) return;
        if (player.getFoodData().getFoodLevel() >= 20) return;
        var below = player.blockPosition().below();
        var state = player.level().getBlockState(below);
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.PODZOL)) {
            // Saturation effect feeds hunger without touching FoodData internals directly.
            grant(player, MobEffects.SATURATION, 1, 0);
        }
    }

    private static void grant(ServerPlayer player, Holder<net.minecraft.world.effect.MobEffect> effect, int ticks, int amp) {
        player.addEffect(new MobEffectInstance(effect, ticks, amp, true, false, true));
    }

    private static void applyAttribute(ServerPlayer player, Holder<Attribute> attribute, Identifier id,
                                       double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, op));
        }
    }

    // ------------------------------------------------------------------
    // Tooltip decoration
    // ------------------------------------------------------------------

    private static void decorate(ItemStack stack, AbilityAccessoryDef2 def) {
        if (isDecorated(stack)) return;
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(def.displayName).withStyle(def.rarity.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(def.powerLabel).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.empty());
        lore.add(Component.literal("Passive: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("works from your inventory").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(def.flavor).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal(def.rarity.display.toUpperCase(java.util.Locale.ROOT) + " "
                + def.type.name()).withStyle(def.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(lore));
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

    private static Item reg(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        Item item = new Item(new Item.Properties().stacksTo(1).setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        ALL.put(name, registered);
        return registered;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
