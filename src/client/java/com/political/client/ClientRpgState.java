package com.political.client;

/** Holds the latest server-synced RPG stats for client HUD rendering. */
public final class ClientRpgState {
    public static volatile float defense;
    public static volatile float strength;
    public static volatile float maxMana = 100f;
    public static volatile float mana = 100f;
    public static volatile float maxCursedEnergy = 0f;
    public static volatile float cursedEnergy = 0f;
    public static volatile float critChance = 0f;
    public static volatile float ferocity = 0f;
    public static volatile float speed = 0f;
    public static volatile int sorcererGrade;
    /** Live JJK aura flags from {@link com.political.net.StatSyncS2C}. */
    public static volatile int jjkFlags;

    private ClientRpgState() {}

    public static boolean jjk(int flag) {
        return (jjkFlags & flag) != 0;
    }
}
