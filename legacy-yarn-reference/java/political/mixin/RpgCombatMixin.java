package com.political.mixin;

import com.political.combat.CombatEngine;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * The single RPG combat hook. Applies the {@link CombatEngine} layer (defense
 * mitigation for players, strength/crit for player attackers) on top of vanilla
 * damage, composing with the existing slayer/attribute damage mixins.
 */
@Mixin(LivingEntity.class)
public abstract class RpgCombatMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float politicalserver$rpgDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof ServerPlayerEntity victim) {
            amount = CombatEngine.modifyIncomingPlayerDamage(victim, source, amount);
        }

        if (!(self instanceof ServerPlayerEntity)
                && source.getAttacker() instanceof ServerPlayerEntity attacker) {
            amount = CombatEngine.modifyOutgoingPlayerDamage(attacker, self, amount);
        }

        return amount;
    }
}
