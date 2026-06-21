package com.political.civics;

import com.mojang.brigadier.CommandDispatcher;
import com.political.economy.AuctionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Single wiring surface for the Expansion 3 civics systems. The integration agent
 * should call these from {@code RpgPoliticsMod}:
 *
 * <ul>
 *   <li>{@code CivicsBootstrap.registerCommands(dispatcher)} inside the
 *       {@code CommandRegistrationCallback}.</li>
 *   <li>{@code CivicsBootstrap.tick(server)} inside {@code END_SERVER_TICK}.</li>
 *   <li>{@code CivicsBootstrap.onEntityKilled(entity, killer)} inside the existing
 *       {@code ServerLivingEntityEvents.AFTER_DEATH} handler (where {@code killer}
 *       is the {@code ServerPlayer} responsible).</li>
 * </ul>
 *
 * All state persists through the existing {@code DataManager}/{@code PoliticsData}
 * save system, so no extra load/save wiring is required.
 */
public final class CivicsBootstrap {

    private CivicsBootstrap() {}

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        CivicsCommands.register(dispatcher);
    }

    public static void tick(MinecraftServer server) {
        JobManager.tick(server);
        LawManager.tick(server);
        OfficeManager.tick(server);
        FactionManager.tick(server);
        TreasuryFund.tick(server);
        AuctionManager.tick(server);
    }

    /** Routes kill events to the wanted-bounty payout (players) and hunter job XP (mobs). */
    public static void onEntityKilled(LivingEntity victim, ServerPlayer killer) {
        if (killer == null) return;
        if (victim instanceof ServerPlayer victimPlayer) {
            JusticeManager.onPlayerKilled(victimPlayer, killer);
        } else {
            JobManager.onBountyKill(killer);
        }
    }
}
