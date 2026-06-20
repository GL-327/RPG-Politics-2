package com.political.combat;

import com.political.court.ConfiscationManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Central RPG combat helpers. Damage scaling is handled via attributes in StatManager. */
public final class CombatEngine {

    private CombatEngine() {}

    /**
     * The Gavel's Execution Strike: a guaranteed lethal blow inside an active
     * Court Domain. Confiscates gear, strips totems so it truly one-shots, then
     * deals lethal damage credited to the judge.
     */
    public static void executeInstantKill(ServerPlayer judge, ServerPlayer accused) {
        var inv = accused.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }

        ConfiscationManager.seize(judge, accused);

        ServerLevel level = accused.level();
        level.sendParticles(ParticleTypes.SONIC_BOOM,
                accused.getX(), accused.getY() + accused.getBbHeight() * 0.5, accused.getZ(),
                1, 0, 0, 0, 0);
        accused.hurtServer(level, level.damageSources().playerAttack(judge), Float.MAX_VALUE);
    }
}
