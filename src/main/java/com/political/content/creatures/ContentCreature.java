package com.political.content.creatures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Shared entity for every {@link CreatureSpecies}: a peaceful/stoic {@link Animal} whose stats and
 * behaviour are spec-driven. Uses only stock vanilla AI goals (float, panic, stroll, look) so it is
 * robust and mixin-free. Breeding is intentionally disabled (no offspring), keeping these as ambient
 * fauna rather than farm animals.
 */
public class ContentCreature extends Animal {

    private final CreatureSpecies species;

    public ContentCreature(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.species = ContentCreatures.speciesForType(type);
    }

    public CreatureSpecies species() {
        return species;
    }

    public static AttributeSupplier.Builder createAttributes(CreatureSpecies species) {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, species.maxHealth)
                .add(Attributes.MOVEMENT_SPEED, species.speed)
                .add(Attributes.SCALE, species.scale);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        if (species != null && species.role == CreatureSpecies.Role.PREY) {
            goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        }
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        if (species != null && species.role == CreatureSpecies.Role.STOIC) {
            // Stoic beasts don't seek targets but will defend themselves once provoked.
            targetSelector.addGoal(1, new HurtByTargetGoal(this));
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        // Ambient fauna — not farmable, so nothing counts as breeding food.
        return false;
    }
}
