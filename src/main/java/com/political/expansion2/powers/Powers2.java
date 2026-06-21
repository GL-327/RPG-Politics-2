package com.political.expansion2.powers;

/** Entry point for Expansion 2 powers. */
public final class Powers2 {

    private Powers2() {}

    public static void register() {
        PowerManager2.register();
    }

    public static void registerClient() {
        // Client hook: PoliticalClient opens PowersScreen2 directly.
    }

    public static java.util.List<String> allIds() {
        return Power2.allIds();
    }

    public static void registerCommands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> d) {
        Power2Commands.register(d);
    }
}
