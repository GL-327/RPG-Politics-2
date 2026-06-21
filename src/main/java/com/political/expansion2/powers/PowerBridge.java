package com.political.expansion2.powers;

import com.political.power.PowerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Integration bridge for Expansion 2 + base power systems. */
public final class PowerBridge {

    private PowerBridge() {}

    public static Component activateSelected(ServerPlayer player) {
        Component p2 = PowerManager2.activateSelected(player);
        if (p2 != null) return p2;
        return PowerManager.activateSelected(player);
    }

    public static void handleAction(ServerPlayer player, String action, String powerId) {
        if (Power2.byId(powerId) != null
                || (Power2.byId(com.political.politics.DataManager.selectedPower(player.getStringUUID())) != null
                    && "activate".equals(action))) {
            PowerManager2.handleAction(player, action, powerId);
        } else {
            PowerManager.handleAction(player, action, powerId);
        }
    }

    public static boolean isExpansionPower(String id) {
        return Power2.byId(id) != null;
    }
}
