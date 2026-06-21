package com.political.npc;

import com.political.net.DialogueChooseC2S;
import com.political.net.DialogueOpenS2C;
import com.political.net.ModNetworking;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side dialogue sessions opened on villager right-click. */
public final class DialogueManager {

    public record Session(UUID villagerId, String villagerName, VillagerManager.Role role, String nodeId) {}

    private static final Map<UUID, Session> ACTIVE = new HashMap<>();

    private DialogueManager() {}

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(entity instanceof Villager villager) || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (sp.isShiftKeyDown()) return InteractionResult.PASS;
            if (com.political.expansion2.npc.Expansion2VillagerHooks.isExpansionNpc(villager.getUUID())) {
                return InteractionResult.PASS;
            }
            open(sp, villager);
            return InteractionResult.SUCCESS;
        });
    }

    public static void open(ServerPlayer player, Villager villager) {
        VillagerManager.Role role = VillagerManager.roleOf(villager.getUUID());
        String name = VillagerManager.nameOf(villager.getUUID());
        DialogueNode node = DialogueTrees.start(role, name);
        Session s = new Session(villager.getUUID(), name, role, node.id());
        ACTIVE.put(player.getUUID(), s);
        ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), name, role, node));
    }

    public static void handleChoice(ServerPlayer player, String choiceId, String action) {
        if (com.political.expansion2.npc.Expansion2DialogueBridge.handleChoice(player, choiceId, action)) return;
        Session s = ACTIVE.get(player.getUUID());
        if (s == null) return;
        Villager villager = findVillager(player, s.villagerId());
        if (villager == null) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        switch (action) {
            case "heal" -> VillagerManager.runHealer(player, villager, s.villagerName());
            case "teach" -> VillagerManager.runSage(player, villager, s.villagerName());
            case "smith" -> VillagerManager.runBlacksmith(player, villager, s.villagerName());
            case "trade" -> VillagerManager.runMerchant(player, villager, s.villagerName());
            case "bless" -> VillagerManager.runGuard(player, villager, s.villagerName());
            case "open_trades" -> {
                ACTIVE.remove(player.getUUID());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Shift + right-click the villager to open trading.").withStyle(net.minecraft.ChatFormatting.GRAY));
                return;
            }
            case "farewell" -> {
                ACTIVE.remove(player.getUUID());
                ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), s.villagerName(),
                        s.role(), DialogueTrees.farewell(s.villagerName(), s.role())));
                return;
            }
            default -> { }
        }
        DialogueNode next = DialogueTrees.result(s.villagerName(), s.role(), "Done.");
        ModNetworking.send(player, DialoguePackets.packet(villager.getUUID(), s.villagerName(), s.role(), next));
    }

    private static Villager findVillager(ServerPlayer player, UUID id) {
        var e = player.level().getEntity(id);
        return e instanceof Villager v ? v : null;
    }
}
