package com.political.expansion2.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Expansion2QuestData {
    public Map<String, String> active = new HashMap<>();
    public Map<String, List<String>> completed = new HashMap<>();
    public Map<String, Map<String, Integer>> bossesDefeated = new HashMap<>();
    public Map<String, Boolean> votedDuringQuest = new HashMap<>();

    public List<String> completedList(String uuid) {
        return completed.computeIfAbsent(uuid, k -> new ArrayList<>());
    }
}
