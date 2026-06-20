package com.political;

import com.political.bounty.BountyCommands;
import com.political.bounty.BountyManager;
import com.political.combat.AbilityEngine;
import com.political.combat.HealthScalingManager;
import com.political.combat.StatManager;
import com.political.court.CourtCommands;
import com.political.court.CourtDomainManager;
import com.political.economy.BankManager;
import com.political.economy.CurrencyCommands;
import com.political.economy.EconomyCommands;
import com.political.economy.MarketCommands;
import com.political.economy.MarketManager;
import com.political.player.PlayerCommands;
import com.political.politics.GovExtrasCommands;
import com.political.items.RpgItemCommands;
import com.political.net.ModNetworking;
import com.political.politics.DataManager;
import com.political.politics.ElectionManager;
import com.political.politics.PerkManager;
import com.political.politics.PoliticsCommands;
import com.political.politics.PrisonManager;
import com.political.politics.TaxManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (client + server) entrypoint for RPG Politics 2 on Minecraft 26.2.
 * Wires up the RPG stat pipeline, networking, the Court Domain system, and the
 * elected-government politics core.
 */
public class RpgPoliticsMod implements ModInitializer {

    public static final String MOD_ID = "politicalserver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModNetworking.registerS2CTypes();
        CourtDomainManager.registerEvents();
        AbilityEngine.register();
        HealthScalingManager.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CourtCommands.register(dispatcher);
            PoliticsCommands.register(dispatcher);
            EconomyCommands.register(dispatcher);
            CurrencyCommands.register(dispatcher);
            BountyCommands.register(dispatcher);
            RpgItemCommands.register(dispatcher);
            PlayerCommands.register(dispatcher);
            MarketCommands.register(dispatcher);
            GovExtrasCommands.register(dispatcher);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof ServerPlayer killer) {
                BountyManager.onEntityDeath(entity, killer);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DataManager.load(server);
            MarketManager.ensureSeeded();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(DataManager::save);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            StatManager.tickAll(server);
            AbilityEngine.tick(server);
            CourtDomainManager.tick(server);
            ElectionManager.tick(server);
            PrisonManager.tick(server);
            TaxManager.tick(server);
            PerkManager.tickPerks(server);
            BankManager.tickInterest(server);
            MarketManager.tick(server);
            BountyManager.tick(server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            DataManager.registerPlayer(player);
            PrisonManager.checkPlayerJoin(player);
            PerkManager.applyActivePerks(player);
            StatManager.apply(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            StatManager.remove(player.getUUID());
            AbilityEngine.onPlayerRemoved(player.getUUID());
            CourtDomainManager.onPlayerRemoved(player.getUUID());
        });

        LOGGER.info("RPG Politics 2 initialized for Minecraft 26.2.");
    }
}
