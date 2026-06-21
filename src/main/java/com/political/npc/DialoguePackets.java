package com.political.npc;

import com.political.net.DialogueOpenS2C;
import com.political.net.ModNetworking;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Builds network payloads from dialogue trees. */
public final class DialoguePackets {

    private DialoguePackets() {}

    public static DialogueOpenS2C packet(UUID villagerId, String name, VillagerManager.Role role, DialogueNode node) {
        List<DialogueOpenS2C.DialogueChoice> choices = new ArrayList<>();
        for (DialogueNode.DialogueChoice c : node.choices()) {
            choices.add(new DialogueOpenS2C.DialogueChoice(c.id(), c.label().getString(), c.action()));
        }
        return DialogueOpenS2C.from(villagerId, name, role.name(), node.id(), node.line().getString(), choices);
    }
}
