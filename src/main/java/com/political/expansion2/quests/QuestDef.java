package com.political.expansion2.quests;

import com.political.expansion2.npc.NpcArchetype;

public record QuestDef(
        String id, String title, String description, QuestKind kind,
        NpcArchetype giver, String target, int amount,
        int coinReward, int creditReward, boolean repeatable
) {}
