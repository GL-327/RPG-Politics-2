package com.political.expansion2;

import com.political.expansion2.npc.Expansion2BossSpawner;
import com.political.expansion2.npc.Expansion2DialogueBridge;
import com.political.expansion2.npc.Expansion2VillagerHooks;
import com.political.expansion2.npc.NpcCommands;
import com.political.expansion2.quests.Expansion2QuestManager;
import com.political.expansion2.quests.Expansion2QuestStorage;
import com.political.expansion2.quests.QuestCommands;
import com.political.expansion2.quests.QuestItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Single entrypoint for Expansion 2 NPC + quest content. Integration agent calls
 * {@link #register()} from {@code RpgPoliticsMod.onInitialize()} and wires dialogue delegate.
 */
public final class Expansion2Bootstrap {

    private static int tickCounter = 0;

    private Expansion2Bootstrap() {}

    public static void register() {
        QuestItems.register();
        Expansion2VillagerHooks.register();
        Expansion2DialogueBridge.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            QuestCommands.register(dispatcher);
            NpcCommands.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(Expansion2QuestStorage::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(Expansion2QuestStorage::save);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof ServerPlayer killer) {
                Expansion2QuestManager.onEntityDeath(entity, killer);
                Expansion2BossSpawner.onEntityDeath(entity, killer);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tickCounter % 40 == 0) {
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    Expansion2QuestManager.tick(p);
                }
            }
            Expansion2BossSpawner.tickEnrage(server);
            if (tickCounter % 6000 == 0) Expansion2QuestStorage.save(server);
        });
    }
}
