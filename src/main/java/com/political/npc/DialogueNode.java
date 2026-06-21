package com.political.npc;

import net.minecraft.network.chat.Component;

import java.util.List;

/** A single node in a branching dialogue tree. */
public record DialogueNode(
        String id,
        Component line,
        List<DialogueChoice> choices
) {
    public record DialogueChoice(String id, Component label, String nextNodeId, String action) {}
}
