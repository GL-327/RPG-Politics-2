package com.political.expansion2.npc;

import com.political.expansion2.quests.Expansion2QuestManager;
import com.political.net.ModNetworking;
import com.political.npc.DialogueNode;
import com.political.npc.DialoguePackets;
import com.political.npc.VillagerManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Expansion2DialogueBridge {

    public record Session(UUID villagerId, String villagerName, NpcArchetype archetype, String nodeId) {}

    private static final Map<UUID, Session> ACTIVE = new HashMap<>();

    private Expansion2DialogueBridge() {}

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(entity instanceof Villager villager) || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (sp.isShiftKeyDown()) return InteractionResult.PASS;
            if (!Expansion2VillagerHooks.isExpansionNpc(villager.getUUID())) return InteractionResult.PASS;
            open(sp, villager);
            return InteractionResult.SUCCESS;
        });
    }

    public static void open(ServerPlayer player, Villager villager) {
        NpcArchetype arch = Expansion2VillagerHooks.archetypeOf(villager.getUUID());
        String name = Expansion2VillagerHooks.nameOf(villager.getUUID());
        DialogueNode node = Expansion2DialogueTrees.start(arch, name);
        ACTIVE.put(player.getUUID(), new Session(villager.getUUID(), name, arch, node.id()));
        ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), name, VillagerManager.Role.MERCHANT, node));
    }

    /** @return true if handled — integration should early-return in DialogueManager.handleChoice */
    public static boolean handleChoice(ServerPlayer player, String choiceId, String action) {
        Session s = ACTIVE.get(player.getUUID());
        if (s == null || action == null || !action.startsWith("exp2:")) return false;

        Villager villager = findVillager(player, s.villagerId());
        if (villager == null) {
            ACTIVE.remove(player.getUUID());
            return true;
        }

        String payload = action.substring(5);
        if ("farewell".equals(payload)) {
            ACTIVE.remove(player.getUUID());
            ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), s.villagerName(), VillagerManager.Role.MERCHANT,
                    Expansion2DialogueTrees.farewell(s.villagerName(), s.archetype())));
            return true;
        }
        if (payload.startsWith("nav:")) {
            DialogueNode next = Expansion2DialogueTrees.nodeById(s.archetype(), s.villagerName(), payload.substring(4));
            ACTIVE.put(player.getUUID(), new Session(s.villagerId(), s.villagerName(), s.archetype(), next.id()));
            ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), s.villagerName(), VillagerManager.Role.MERCHANT, next));
            return true;
        }
        if (payload.startsWith("svc:")) {
            String msg = Expansion2NpcServices.run(player, villager, s.archetype(), payload.substring(4), s.villagerName());
            ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), s.villagerName(), VillagerManager.Role.MERCHANT,
                    Expansion2DialogueTrees.result(s.villagerName(), s.archetype(), msg)));
            return true;
        }
        if (payload.startsWith("quest:")) {
            String sub = payload.substring(6);
            String msg = "list".equals(sub) ? Expansion2QuestManager.listOffersFor(player, s.archetype())
                    : "turnin".equals(sub) ? Expansion2QuestManager.tryTurnIn(player, s.archetype())
                    : sub.startsWith("accept:") ? Expansion2QuestManager.accept(player, sub.substring(7))
                    : "Unknown contract action.";
            ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), s.villagerName(), VillagerManager.Role.MERCHANT,
                    Expansion2DialogueTrees.result(s.villagerName(), s.archetype(), msg)));
            return true;
        }
        if ("boss:hint".equals(payload)) {
            NamedNpcBoss b = Expansion2BossSpawner.hintForArchetype(s.archetype());
            String msg = b != null ? "Seek " + b.displayName + ". /exp2quest boss " + b.id : "No marked boss.";
            ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), s.villagerName(), VillagerManager.Role.MERCHANT,
                    Expansion2DialogueTrees.result(s.villagerName(), s.archetype(), msg)));
            return true;
        }
        return true;
    }

    private static Villager findVillager(ServerPlayer player, UUID id) {
        var e = player.level().getEntity(id);
        return e instanceof Villager v ? v : null;
    }
}
