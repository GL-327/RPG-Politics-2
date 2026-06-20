package com.political.combat;

import com.political.court.ConfiscationManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

/**
 * Central RPG combat pipeline. Other systems (Court Domain, items, perks) route
 * through here instead of each adding their own damage mixin. This sits on top
 * of vanilla armor as an additional RPG layer rather than fighting it.
 */
public final class CombatEngine {

    private static final Random RNG = new Random();

    private CombatEngine() {}

    /** Reduces incoming damage to a player by their RPG Defense stat. */
    public static float modifyIncomingPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        RpgStats s = StatManager.get(player);
        if (s == null || s.defense <= 0) return amount;
        // Diminishing-returns mitigation: 100 defense = 50% reduction.
        double multiplier = 1.0 - (s.defense / (s.defense + 100.0));
        return (float) (amount * multiplier);
    }

    /** Scales a player's outgoing damage by Strength, with a chance to crit. */
    public static float modifyOutgoingPlayerDamage(ServerPlayerEntity player, LivingEntity target, float amount) {
        RpgStats s = StatManager.get(player);
        if (s == null) return amount;
        double damage = amount * (1.0 + s.strength / 100.0);
        if (s.critChance > 0 && RNG.nextDouble() * 100.0 < s.critChance) {
            damage *= (1.0 + s.critDamage / 100.0);
            if (player.getServerWorld() != null) {
                player.getServerWorld().spawnParticles(net.minecraft.particle.ParticleTypes.CRIT,
                        target.getX(), target.getBodyY(0.5), target.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            }
        }
        return (float) damage;
    }

    /**
     * The Gavel's Execution Strike: an unavoidable lethal blow used inside an
     * active Court Domain. Confiscates the target's gear, strips any totems so
     * the strike truly one-shots, then deals lethal damage credited to the judge.
     */
    public static void executeInstantKill(ServerPlayerEntity judge, ServerPlayerEntity accused) {
        var inv = accused.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                inv.setStack(i, net.minecraft.item.ItemStack.EMPTY);
            }
        }

        ConfiscationManager.seize(judge, accused);

        ServerWorld world = accused.getServerWorld();
        world.spawnParticles(net.minecraft.particle.ParticleTypes.SONIC_BOOM,
                accused.getX(), accused.getBodyY(0.5), accused.getZ(), 1, 0, 0, 0, 0);
        accused.damage(world, world.getDamageSources().playerAttack(judge), Float.MAX_VALUE);
    }
}
