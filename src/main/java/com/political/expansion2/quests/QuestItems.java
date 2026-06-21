package com.political.expansion2.quests;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestItems {

    public static final String MOD_ID = "politicalserver";
    private static final Map<String, Item> ALL = new LinkedHashMap<>();

    private QuestItems() {}

    public static void register() {
        item("quest2_bounty_seal");
        item("quest2_cursed_relic");
        item("quest2_grimoire_page");
        item("quest2_shrine_offering");
        item("quest2_map_fragment");
        item("quest2_election_ballot");
        item("quest2_spirit_tag");
        item("quest2_boss_token");
        item("quest2_bank_ledger");
        item("quest2_herb_satchel");
        item("quest2_rune_shard");
        item("quest2_merc_contract");
        item("quest2_cursed_coin");
        item("quest2_awakening_stone");
        item("quest2_diplomat_seal");
    }

    public static Item get(String id) { return ALL.get(id); }
    public static List<Item> items() { return new ArrayList<>(ALL.values()); }

    private static void item(String id) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, id));
        Item it = new Item(new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, it);
        ALL.put(id, it);
    }
}
