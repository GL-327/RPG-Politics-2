package com.political.client;

/** Holds the latest server-synced RPG stats for client HUD rendering. */
public final class ClientRpgState {
    public static volatile float defense;
    public static volatile float strength;
    public static volatile float maxMana = 100f;
    public static volatile float mana = 100f;

    private ClientRpgState() {}
}
