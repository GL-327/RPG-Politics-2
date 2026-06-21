package com.political.curse;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Registers the custom Curse entity type + attributes. */
public final class ModEntities {

    public static final String MOD_ID = "politicalserver";

    public static EntityType<CurseEntity> CURSE_SPIRIT;

    private ModEntities() {}

    public static void register() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "curse_spirit"));
        CURSE_SPIRIT = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(CurseEntity::new, MobCategory.MONSTER)
                        .sized(0.7f, 2.3f)
                        .build(key));
        FabricDefaultAttributeRegistry.register(CURSE_SPIRIT, CurseEntity.createAttributes());
    }
}
