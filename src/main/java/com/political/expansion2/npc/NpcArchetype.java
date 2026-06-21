package com.political.expansion2.npc;

import net.minecraft.ChatFormatting;

/** 35 unique villager archetypes for Expansion 2 NPC content. */
public enum NpcArchetype {
    BLACKSMITH("Blacksmith", ChatFormatting.GOLD, "Steel and sorcery — the forge never cools."),
    ENCHANTER("Enchanter", ChatFormatting.LIGHT_PURPLE, "Runes whisper secrets to those who listen."),
    SORCERER("Sorcerer", ChatFormatting.DARK_PURPLE, "Cursed energy flows through all things."),
    BOUNTY_BROKER("Bounty Broker", ChatFormatting.RED, "Coin for blood — a fair trade in these lands."),
    BANKER("Banker", ChatFormatting.GREEN, "Your wealth is safe… for a modest fee."),
    POLITICIAN("Politician", ChatFormatting.YELLOW, "Vote for progress. Or at least vote for me."),
    HEALER("Healer", ChatFormatting.AQUA, "Wounds mend; spirits linger longer."),
    CURSED_MERCHANT("Cursed Goods Merchant", ChatFormatting.DARK_RED, "Tainted wares for bold souls only."),
    ARMOR_SMITH("Armor Smith", ChatFormatting.GRAY, "Plate, mail, and warded leather — pick your fate."),
    WEAPON_SMITH("Weapon Smith", ChatFormatting.GOLD, "Every blade tells a story. Yours starts now."),
    ALCHEMIST("Alchemist", ChatFormatting.DARK_AQUA, "Brews that burn, heal, or reveal."),
    SCOUT("Scout", ChatFormatting.DARK_GREEN, "I've seen things beyond the settlement walls."),
    MERCENARY("Mercenary", ChatFormatting.DARK_GRAY, "Pay me enough and your enemies become statistics."),
    TAX_COLLECTOR("Tax Collector", ChatFormatting.GOLD, "The treasury hungers. Feed it."),
    ELECTION_CLERK("Election Clerk", ChatFormatting.WHITE, "Ballots, seals, and civic duty."),
    SPIRIT_HUNTER("Spirit Hunter", ChatFormatting.DARK_PURPLE, "I track curses by their footprints."),
    CURSE_SCHOLAR("Curse Scholar", ChatFormatting.BLUE, "Every curse has a thesis. I write them."),
    RUNE_CARVER("Rune Carver", ChatFormatting.DARK_BLUE, "Glyphs cut deep — in stone and flesh."),
    GEM_CUTTER("Gem Cutter", ChatFormatting.AQUA, "Facets catch light; curses catch souls."),
    HERBALIST("Herbalist", ChatFormatting.GREEN, "Roots, petals, and a pinch of luck."),
    INNKEEPER("Innkeeper", ChatFormatting.YELLOW, "Rest your bones. Rumors are free."),
    FENCE("Fence", ChatFormatting.DARK_GRAY, "No questions. No receipts."),
    LOAN_SHARK("Loan Shark", ChatFormatting.RED, "Interest compounds faster than regret."),
    AUCTIONEER("Auctioneer", ChatFormatting.GOLD, "Going once — going twice — sold to despair!"),
    TOWN_CRIER("Town Crier", ChatFormatting.WHITE, "Hear ye! The settlement trembles!"),
    DIPLOMAT("Diplomat", ChatFormatting.BLUE, "Words are weapons when wielded with grace."),
    WARDEN("Warden", ChatFormatting.DARK_RED, "Order is maintained. Break it and pay."),
    EXORCIST("Exorcist", ChatFormatting.LIGHT_PURPLE, "Spirits fear my bell and my blade."),
    SHRINE_KEEPER("Shrine Keeper", ChatFormatting.GOLD, "Offerings appease what lurks below."),
    BEAST_TAMER("Beast Tamer", ChatFormatting.GREEN, "Even monsters crave kindness… sometimes."),
    MAPMAKER("Mapmaker", ChatFormatting.GRAY, "Every path I've charted ends in danger."),
    RELIC_DEALER("Relic Dealer", ChatFormatting.DARK_PURPLE, "Antiquities with appetites of their own."),
    GRIMOIRE_SELLER("Grimoire Seller", ChatFormatting.DARK_RED, "Pages that read you back."),
    SUMMONER("Summoner", ChatFormatting.LIGHT_PURPLE, "I call what the world forgot."),
    DOOMSDAY_PROPHET("Doomsday Prophet", ChatFormatting.DARK_RED, "The end approaches. Buy candles.");

    public final String title;
    public final ChatFormatting color;
    public final String tagline;

    NpcArchetype(String title, ChatFormatting color, String tagline) {
        this.title = title;
        this.color = color;
        this.tagline = tagline;
    }

    public static NpcArchetype forVillager(java.util.UUID id) {
        NpcArchetype[] all = values();
        return all[Math.floorMod(id.hashCode(), all.length)];
    }
}
