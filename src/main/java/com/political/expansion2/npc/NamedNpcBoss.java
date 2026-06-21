package com.political.expansion2.npc;

import net.minecraft.ChatFormatting;

/** 12 named NPC bosses spawned as enhanced vindicators. */
public enum NamedNpcBoss {
    VALDRIS("valdris", "Iron Merchant Valdris", ChatFormatting.GOLD, 180, 14, 0.28),
    MORGRIM("morgrim", "Cursed Knight Morgrim", ChatFormatting.DARK_RED, 220, 16, 0.26),
    SYLVA("sylva", "Shadow Enchanter Sylva", ChatFormatting.DARK_PURPLE, 200, 15, 0.30),
    RAZIEL("raziel", "Bounty King Raziel", ChatFormatting.RED, 240, 17, 0.27),
    CROFT("croft", "Bank Heist Mastermind Croft", ChatFormatting.GREEN, 190, 13, 0.32),
    BLACKWOOD("blackwood", "Corrupt Senator Blackwood", ChatFormatting.YELLOW, 210, 12, 0.25),
    MORBIDIUS("morbidius", "Plague Healer Morbidius", ChatFormatting.DARK_GREEN, 230, 15, 0.24),
    ASHARA("ashara", "Soul Binder Ashara", ChatFormatting.LIGHT_PURPLE, 250, 18, 0.29),
    GARRICK("garrick", "Arena Champion Garrick", ChatFormatting.GOLD, 260, 19, 0.27),
    KAGURO("kaguro", "Spirit Lord Kaguro", ChatFormatting.DARK_PURPLE, 280, 20, 0.26),
    THORN("thorn", "Election Rigging Vizier Thorn", ChatFormatting.WHITE, 200, 14, 0.28),
    PYRION("pyrion", "Ancient Sorcerer Pyrion", ChatFormatting.DARK_RED, 300, 22, 0.25);

    public final String id;
    public final String displayName;
    public final ChatFormatting color;
    public final double baseHealth;
    public final double baseDamage;
    public final double moveSpeed;

    NamedNpcBoss(String id, String displayName, ChatFormatting color,
                 double baseHealth, double baseDamage, double moveSpeed) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.baseHealth = baseHealth;
        this.baseDamage = baseDamage;
        this.moveSpeed = moveSpeed;
    }

    public static NamedNpcBoss byId(String id) {
        for (NamedNpcBoss b : values()) if (b.id.equalsIgnoreCase(id)) return b;
        return null;
    }
}
