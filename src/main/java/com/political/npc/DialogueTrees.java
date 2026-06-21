package com.political.npc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

/** Data-driven Fallout-style dialogue trees per villager role. */
public final class DialogueTrees {

    private DialogueTrees() {}

    public static DialogueNode start(VillagerManager.Role role, String villagerName) {
        return switch (role) {
            case HEALER -> node("greet",
                    say(villagerName, "Ah, traveller. You look weary. Shall I mend your wounds?", role),
                    List.of(
                            choice("Heal me", "heal", "heal"),
                            choice("Just passing through", "bye", "farewell")
                    ));
            case SAGE -> node("greet",
                    say(villagerName, "The cursed energy within you stirs... seek knowledge?", role),
                    List.of(
                            choice("Teach me a technique", "teach", "teach"),
                            choice("Not today", "bye", "farewell")
                    ));
            case BLACKSMITH -> node("greet",
                    say(villagerName, "Steel and sorcery — the forge is hot. Need a weapon?", role),
                    List.of(
                            choice("Show me your wares", "smith", "smith"),
                            choice("Maybe later", "bye", "farewell")
                    ));
            case MERCHANT -> node("greet",
                    say(villagerName, "Coin flows where courage goes. Care for a trade?", role),
                    List.of(
                            choice("Any deals today?", "trade", "trade"),
                            choice("Farewell", "bye", "farewell")
                    ));
            case GUARD -> node("greet",
                    say(villagerName, "Stand tall, citizen. The settlement watches over you.", role),
                    List.of(
                            choice("Bless me for battle", "bless", "bless"),
                            choice("Open trading", "trade_menu", "open_trades"),
                            choice("Leave", "bye", "farewell")
                    ));
        };
    }

    public static DialogueNode farewell(String villagerName, VillagerManager.Role role) {
        return node("farewell", say(villagerName, "Safe travels.", role), List.of());
    }

    public static DialogueNode result(String villagerName, VillagerManager.Role role, String text) {
        return node("result", say(villagerName, text, role),
                List.of(choice("Goodbye", "bye", "farewell")));
    }

    private static DialogueNode node(String id, Component line, List<DialogueNode.DialogueChoice> choices) {
        return new DialogueNode(id, line, choices);
    }

    private static DialogueNode.DialogueChoice choice(String label, String id, String action) {
        return new DialogueNode.DialogueChoice(id, Component.literal(label).withStyle(ChatFormatting.WHITE), id, action);
    }

    private static Component say(String name, String text, VillagerManager.Role role) {
        return Component.literal(name + ": ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(text).withStyle(role.color));
    }
}
