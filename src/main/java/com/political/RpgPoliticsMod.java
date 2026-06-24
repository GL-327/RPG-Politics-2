package com.political;

import com.political.bounty.BountyCommands;
import com.political.bounty.BountyManager;
import com.political.items.ItemActiveAbilityEngine;
import com.political.combat.AbilityEngine;
import com.political.combat.StatManager;
import com.political.curse.CurseCommands;
import com.political.curse.CurseManager;
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
import com.political.npc.VillagerManager;
import com.political.power.ModItems;
import com.political.power.PowerCommands;
import com.political.power.PowerManager;
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
        // Load tunables from config/politicalserver.json BEFORE anything reads them: mob attribute
        // scaling and natural-spawn flags are consumed at type registration further down.
        com.political.config.PoliticalConfig.load();
        com.political.sound.ModSounds.register();
        com.political.vfx.VfxBootstrap.init();
        ModItems.register();
        com.political.content.ModBlocks.register();
        ModNetworking.registerS2CTypes();
        ModNetworking.registerC2STypes();
        CourtDomainManager.registerEvents();
        AbilityEngine.register();
        ItemActiveAbilityEngine.register();
        PowerManager.register();
        com.political.flight.FlightManager.register();
        com.political.curse.ModEntities.register();
        com.political.expansion2.Expansion2Bootstrap.register();
        com.political.expansion.mobs.ExpansionMobs.register();
        com.political.expansion2.mobs.ExpansionMobs2.register();
        com.political.expansion.melee.MeleeWeapons.register();
        com.political.expansion2.melee.Melee2Weapons.register();
        com.political.expansion.ranged.RangedExpansion.register();
        com.political.expansion2.ranged.RangedExpansion2.register();
        com.political.expansion.armor.ArmorExpansion.register();
        com.political.expansion2.armor.Armor2Expansion.register();
        com.political.expansion.accessories.Accessories.register();
        com.political.expansion2.accessories.Accessories2.register();
        com.political.expansion.blocks.DecoBlocks.register();
        com.political.expansion2.blocks.DecoBlocks2.register();
        com.political.expansion.food.FoodItems.register();
        com.political.expansion2.food.Food2Items.register();
        com.political.expansion2.curses.CurseSpirits2.register();
        com.political.expansion2.powers.Powers2.register();
        CurseManager.register();
        com.political.curse.CursedObjects.register();
        com.political.curse.CursedGear.register();
        com.political.items.RelicItems.register();
        com.political.echo.EchoItems.register();
        com.political.gov.GovItems.register();
        com.political.dev.DevMenuItem.register();
        com.political.content.ModTabs.register();
        VillagerManager.register();
        com.political.npc.DialogueManager.register();
        com.political.world.SettlementManager.register();
        com.political.world.dungeons.DungeonRegistry.register();
        com.political.world.structures.StructureRegistry.register();
        com.political.guide.GuideRegistry.register();
        com.political.progression.ProgressionManager.register();
        com.political.content.ContentBootstrap.init();
        com.political.curse.JjkBootstrap.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CourtCommands.register(dispatcher);
            PoliticsCommands.register(dispatcher);
            EconomyCommands.register(dispatcher);
            CurrencyCommands.register(dispatcher);
            BountyCommands.register(dispatcher);
            RpgItemCommands.register(dispatcher);
            com.political.items.ItemEditorCommands.register(dispatcher);
            PlayerCommands.register(dispatcher);
            MarketCommands.register(dispatcher);
            GovExtrasCommands.register(dispatcher);
            PowerCommands.register(dispatcher);
            CurseCommands.register(dispatcher);
            com.political.expansion.mobs.MobCommands.register(dispatcher);
            com.political.expansion2.mobs.MobCommands2.register(dispatcher);
            com.political.expansion2.powers.Powers2.registerCommands(dispatcher);
            com.political.world.SettlementCommands.register(dispatcher);
            com.political.world.dungeons.DungeonCommands.register(dispatcher);
            com.political.world.structures.StructureCommands.register(dispatcher);
            com.political.world.structures.ContentStructureCommands.register(dispatcher);
            com.political.config.ConfigCommands.register(dispatcher);
            com.political.civics.CivicsBootstrap.registerCommands(dispatcher);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof ServerPlayer killer) {
                BountyManager.onEntityDeath(entity, killer);
                CurseManager.onEntityDeath(entity, killer);
                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    com.political.civics.CivicsBootstrap.onEntityKilled(living, killer);
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DataManager.load(server);
            MarketManager.ensureSeeded();
            com.political.world.SettlementManager.onServerStarted(server);
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
            PowerManager.tick(server);
            CurseManager.tick(server);
            com.political.world.SettlementManager.tick(server);
            com.political.world.dungeons.DungeonManager.tick(server);
            com.political.world.structures.StructureManager.tick(server);
            com.political.world.structures.ContentStructureManager.tick(server);
            com.political.civics.CivicsBootstrap.tick(server);

            // Periodic autosave (every 5 minutes) so progression survives crashes.
            if (server.getTickCount() % 6000 == 0) DataManager.save(server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            String uuid = player.getStringUUID();
            boolean firstJoin = !DataManager.data().playerNames.containsKey(uuid);
            DataManager.registerPlayer(player);
            DataManager.ensureTrait(uuid);
            PrisonManager.checkPlayerJoin(player);
            PerkManager.applyActivePerks(player);
            StatManager.apply(player);
            com.political.world.SettlementManager.ensureCitizenship(player);
            if (firstJoin) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Welcome to RPG Politics 2. G = techniques, F = flight, K = powers, R = activate, /guide = field manual.")
                        .withStyle(net.minecraft.ChatFormatting.GOLD));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            StatManager.remove(player.getUUID());
            com.political.curse.rules.JjkRules.clear(player.getUUID());
            AbilityEngine.onPlayerRemoved(player.getUUID());
            PowerManager.onPlayerRemoved(player.getUUID());
            com.political.expansion2.powers.PowerManager2.onPlayerRemoved(player.getUUID());
            CourtDomainManager.onPlayerRemoved(player.getUUID());
        });

        LOGGER.info("RPG Politics 2 initialized for Minecraft 26.2.");
    }
}
