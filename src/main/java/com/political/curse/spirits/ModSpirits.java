package com.political.curse.spirits;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Registers one {@code EntityType} (+ default attributes) per {@link SpiritSpecies} and provides the
 * species/type lookups used by {@link CursedSpiritEntity} and the curse spawn system. Called from
 * {@code ModEntities.register()} which is already wired into mod init, so no extra wiring is needed
 * server-side. Client renderers are bound separately via {@code SpiritClient.registerClient()}.
 */
public final class ModSpirits {

    public static final String MOD_ID = "politicalserver";

    private static final Map<SpiritSpecies, EntityType<CursedSpiritEntity>> TYPE_BY_SPECIES =
            new EnumMap<>(SpiritSpecies.class);
    private static final Map<EntityType<?>, SpiritSpecies> SPECIES_BY_TYPE = new HashMap<>();
    private static final Random RNG = new Random();

    private ModSpirits() {}

    public static void registerAll() {
        if (!TYPE_BY_SPECIES.isEmpty()) return; // idempotent
        for (SpiritSpecies sp : SpiritSpecies.values()) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(MOD_ID, sp.id()));
            EntityType<CursedSpiritEntity> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                    EntityType.Builder.of(CursedSpiritEntity::new, MobCategory.MONSTER)
                            .sized(sp.hitboxWidth(), sp.hitboxHeight())
                            .build(key));
            FabricDefaultAttributeRegistry.register(type, attributesFor(sp));
            TYPE_BY_SPECIES.put(sp, type);
            SPECIES_BY_TYPE.put(type, sp);
        }
    }

    /** Base attribute sheet for a species (grade scaling is layered on later in CurseManager). */
    public static AttributeSupplier.Builder attributesFor(SpiritSpecies sp) {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, sp.baseHealth())
                .add(Attributes.ATTACK_DAMAGE, sp.baseDamage())
                .add(Attributes.MOVEMENT_SPEED, sp.moveSpeed())
                .add(Attributes.FOLLOW_RANGE, sp.boss() ? 56.0 : 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, sp.knockbackResist())
                .add(Attributes.SCALE, sp.baseScale());
    }

    public static EntityType<CursedSpiritEntity> typeFor(SpiritSpecies sp) {
        return TYPE_BY_SPECIES.get(sp);
    }

    public static SpiritSpecies speciesFor(EntityType<?> type) {
        return SPECIES_BY_TYPE.get(type);
    }

    public static boolean isReady() {
        return !TYPE_BY_SPECIES.isEmpty();
    }

    /**
     * Picks a species suited to a curse grade (1..5). Prefers species whose natural band matches the
     * grade; bosses (Special Grade) are weighted rarer so they don't flood grade-5 manifestations.
     */
    public static SpiritSpecies randomForGrade(int grade) {
        grade = Math.max(1, Math.min(5, grade));
        List<SpiritSpecies> pool = new ArrayList<>();
        for (SpiritSpecies sp : SpiritSpecies.values()) {
            if (sp.gradeBand() != grade) continue;
            int weight = sp.boss() ? 1 : 4;
            for (int i = 0; i < weight; i++) pool.add(sp);
        }
        if (pool.isEmpty()) {
            // Fall back to the nearest lower band that has members.
            for (int g = grade; g >= 1 && pool.isEmpty(); g--) {
                for (SpiritSpecies sp : SpiritSpecies.values()) {
                    if (sp.gradeBand() == g && !sp.boss()) pool.add(sp);
                }
            }
        }
        if (pool.isEmpty()) return SpiritSpecies.CURSE_WISP;
        return pool.get(RNG.nextInt(pool.size()));
    }
}
