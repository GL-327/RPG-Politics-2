package com.political.expansion2.curses;

import com.political.combat.StatManager;
import com.political.curse.CurseEntity;
import com.political.curse.CursedObjects;
import com.political.politics.DataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Phase-2 cursed spirit registry and spawn API (90 species, ids prefixed {@code spirit2_}).
 *
 * <p>Integration must call {@link #register()} from the common initializer and
 * {@code com.political.expansion2.curses.Spirit2Client#registerClient()} from the client initializer.
 */
public final class CurseSpirits2 {

    public static final String MOD_ID = "politicalserver";

    private static final Map<SpiritSpecies2, EntityType<CursedSpirit2Entity>> TYPE_BY_SPECIES =
            new EnumMap<>(SpiritSpecies2.class);
    private static final Map<EntityType<?>, SpiritSpecies2> SPECIES_BY_TYPE = new HashMap<>();
    private static final Random RNG = new Random();
    private static boolean registered = false;

    private CurseSpirits2() {}

    public static void register() {
        if (registered) return;
        registered = true;
        registerAll();
        registerDeathRewards();
    }

    private static void registerAll() {
        if (!TYPE_BY_SPECIES.isEmpty()) return;
        for (SpiritSpecies2 sp : SpiritSpecies2.values()) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(MOD_ID, sp.id()));
            EntityType<CursedSpirit2Entity> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                    EntityType.Builder.of(CursedSpirit2Entity::new, MobCategory.MONSTER)
                            .sized(sp.hitboxWidth(), sp.hitboxHeight())
                            .build(key));
            FabricDefaultAttributeRegistry.register(type, attributesFor(sp));
            TYPE_BY_SPECIES.put(sp, type);
            SPECIES_BY_TYPE.put(type, sp);
        }
    }

    public static AttributeSupplier.Builder attributesFor(SpiritSpecies2 sp) {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, sp.baseHealth())
                .add(Attributes.ATTACK_DAMAGE, sp.baseDamage())
                .add(Attributes.MOVEMENT_SPEED, sp.moveSpeed())
                .add(Attributes.FOLLOW_RANGE, sp.boss() ? 56.0 : 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, sp.knockbackResist())
                .add(Attributes.SCALE, sp.baseScale());
    }

    public static EntityType<CursedSpirit2Entity> typeFor(SpiritSpecies2 sp) {
        return TYPE_BY_SPECIES.get(sp);
    }

    public static SpiritSpecies2 speciesFor(EntityType<?> type) {
        return SPECIES_BY_TYPE.get(type);
    }

    public static boolean isReady() {
        return !TYPE_BY_SPECIES.isEmpty();
    }

    public static List<SpiritSpecies2> allTypes() {
        return Collections.unmodifiableList(List.of(SpiritSpecies2.values()));
    }

    public static SpiritSpecies2 randomForGrade(int grade) {
        grade = Math.max(1, Math.min(5, grade));
        List<SpiritSpecies2> pool = new ArrayList<>();
        for (SpiritSpecies2 sp : SpiritSpecies2.values()) {
            if (sp.gradeBand() != grade) continue;
            int weight = sp.boss() ? 1 : 4;
            for (int i = 0; i < weight; i++) pool.add(sp);
        }
        if (pool.isEmpty()) {
            for (int g = grade; g >= 1 && pool.isEmpty(); g--) {
                for (SpiritSpecies2 sp : SpiritSpecies2.values()) {
                    if (sp.gradeBand() == g && !sp.boss()) pool.add(sp);
                }
            }
        }
        if (pool.isEmpty()) return SpiritSpecies2.MOTE_FLEA;
        return pool.get(RNG.nextInt(pool.size()));
    }

    public static CursedSpirit2Entity spawnAt(ServerLevel level, BlockPos pos, int grade) {
        registerAll();
        return spawnSpeciesAt(level, pos, grade, randomForGrade(grade));
    }

    public static CursedSpirit2Entity spawnSpeciesAt(ServerLevel level, BlockPos pos, int grade, SpiritSpecies2 species) {
        registerAll();
        EntityType<CursedSpirit2Entity> type = typeFor(species);
        if (type == null) return null;
        CursedSpirit2Entity curse = type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (curse == null) return null;
        curse.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        manifest(curse, grade);
        level.addFreshEntity(curse);
        return curse;
    }

    public static void manifest(CurseEntity curse, int grade) {
        grade = Math.max(1, Math.min(5, grade));
        curse.setGrade(grade);
        SpiritSpecies2 species = curse instanceof CursedSpirit2Entity c ? c.species() : null;

        double hpMult = species != null ? 1.0 + grade * 0.35 : 1.0 + grade * 0.8;
        double dmgMult = species != null ? 1.0 + grade * 0.12 : 1.0 + grade * 0.45;

        AttributeInstance health = curse.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) { health.setBaseValue(health.getBaseValue() * hpMult); curse.setHealth(curse.getMaxHealth()); }
        AttributeInstance dmg = curse.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(dmg.getBaseValue() * dmgMult);
        AttributeInstance sc = curse.getAttribute(Attributes.SCALE);
        if (sc != null && species == null) sc.setBaseValue(0.85f + grade * 0.18f);
        AttributeInstance kb = curse.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null) kb.setBaseValue(Math.min(1.0, kb.getBaseValue() + grade * 0.08));

        ChatFormatting color = species != null ? species.nameColor()
                : grade >= 5 ? ChatFormatting.DARK_RED : grade >= 4 ? ChatFormatting.RED
                : grade >= 3 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
        String label = species != null ? species.displayName() + " \u00b7 " + gradeLabel(grade) : gradeLabel(grade);
        curse.setCustomName(Component.literal("\u2620 " + label).withStyle(color));
        curse.setCustomNameVisible(species != null && species.boss());
        if (species != null && species.boss()) curse.setPersistenceRequired();

        curse.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        if (grade >= 4) curse.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, false));
        if (grade >= 5) curse.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        if (species != null && species.has(Behavior2.FIRE_IMMUNE)) {
            curse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        }
        if (species != null && species.has(Behavior2.FAST_SWARM)) {
            curse.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, false));
        }
        curse.addTag("rpg_curse2_" + grade);
        if (species != null) curse.addTag("rpg_curse2_" + species.id());
    }

    public static String gradeLabel(int grade) {
        return switch (grade) {
            case 1 -> "Grade 4 Curse";
            case 2 -> "Grade 3 Curse";
            case 3 -> "Grade 2 Curse";
            case 4 -> "Grade 1 Curse";
            case 5 -> "Special Grade Curse";
            default -> "Cursed Spirit";
        };
    }

    public static boolean isSpirit2(Entity entity) {
        return entity instanceof CursedSpirit2Entity;
    }

    private static void registerDeathRewards() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof CursedSpirit2Entity c)) return;
            if (!(source.getEntity() instanceof ServerPlayer killer)) return;
            onExorcised(c, killer);
        });
    }

    private static void onExorcised(CursedSpirit2Entity dead, ServerPlayer killer) {
        int grade = dead.getGrade();
        SpiritSpecies2 species = dead.species();
        int tier = species.lootTier();
        boolean boss = species.boss();

        double energy = 10 + grade * 7 + tier * 5;
        int coins = 15 + grade * 20 + tier * 12;
        if (boss) { energy *= 2.2; coins *= 2; }

        int gained = (int) StatManager.addCursedEnergy(killer, energy);
        DataManager.addCoins(killer.getStringUUID(), coins);
        int total = DataManager.addExorcism(killer.getStringUUID());
        killer.sendSystemMessage(Component.literal("Exorcised " + species.displayName()
                + "! +" + gained + " Cursed Energy, +" + coins + " coins. (" + total + " exorcised)")
                .withStyle(boss ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE));

        if (dead.level() instanceof ServerLevel level) {
            int drops = boss ? 2 + RNG.nextInt(4) : (RNG.nextFloat() < 0.12f + tier * 0.08f ? 1 : 0);
            for (int i = 0; i < drops; i++) {
                var base = boss ? net.minecraft.world.item.Items.WITHER_SKELETON_SKULL : net.minecraft.world.item.Items.BONE;
                var remnant = new net.minecraft.world.item.ItemStack(base);
                CursedObjects.makeCursed(remnant, 18 + grade * 12 + tier * 10);
                level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                        level, dead.getX(), dead.getY() + 0.5, dead.getZ(), remnant));
            }
            if (boss) {
                var star = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHER_STAR);
                CursedObjects.makeCursed(star, 90 + grade * 25);
                level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                        level, dead.getX(), dead.getY() + 0.7, dead.getZ(), star));
            }
        }
    }
}
