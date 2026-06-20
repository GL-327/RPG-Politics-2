package com.political.power;

import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Random;

/**
 * The Compound&nbsp;V serum logic, shared by {@code /v} commands and the serum items.
 * Compound&nbsp;V grants a permanent random power; Temp&nbsp;V grants a temporary one;
 * V1 is an unstable dose; Anti-V strips powers away.
 */
public final class Serums {

    private static final Random RNG = new Random();
    private static final long TEMP_V_DURATION_MS = 5 * 60 * 1000L;

    private Serums() {}

    private static Power randomCompoundV(String uuid, boolean onlyNew) {
        List<Power> pool = Power.ofOrigin(Power.Origin.COMPOUND_V);
        if (onlyNew) pool.removeIf(p -> DataManager.hasPower(uuid, p.id()));
        if (pool.isEmpty()) return null;
        return pool.get(RNG.nextInt(pool.size()));
    }

    /** Permanent random Compound V power. Most players survive... most. */
    public static Component drinkCompoundV(ServerPlayer player) {
        String uuid = player.getStringUUID();
        Power power = randomCompoundV(uuid, true);
        if (power == null) return msg(player, "Compound V courses through you, but you already command every power.", ChatFormatting.GRAY);
        DataManager.grantPower(uuid, power.id());
        DataManager.data().tempPowerExpiry.remove(uuid); // make permanent if previously temp
        return msg(player, "Compound V surges through your veins! You manifest: " + power.displayName + ".", ChatFormatting.RED);
    }

    /** Temporary random Compound V power that expires after a few minutes. */
    public static Component drinkTempV(ServerPlayer player) {
        String uuid = player.getStringUUID();
        Power power = randomCompoundV(uuid, false);
        if (power == null) return msg(player, "Nothing happens.", ChatFormatting.GRAY);
        DataManager.grantPower(uuid, power.id());
        DataManager.setSelectedPower(uuid, power.id());
        DataManager.data().tempPowerExpiry.put(uuid, System.currentTimeMillis() + TEMP_V_DURATION_MS);
        return msg(player, "Temp V kicks in: " + power.displayName + " (5 minutes).", ChatFormatting.GOLD);
    }

    /** V1: the original unstable compound. High reward, real risk. */
    public static Component drinkV1(ServerPlayer player) {
        String uuid = player.getStringUUID();
        if (RNG.nextFloat() < 0.25f) {
            player.igniteForSeconds(4);
            player.hurtServer(player.level(), player.level().damageSources().magic(), 8.0f);
            return msg(player, "The V1 rejects you \u2014 your body convulses violently!", ChatFormatting.DARK_RED);
        }
        Power power = randomCompoundV(uuid, true);
        if (power == null) return msg(player, "The V1 finds nothing new to awaken.", ChatFormatting.GRAY);
        DataManager.grantPower(uuid, power.id());
        return msg(player, "The raw V1 awakens something primal: " + power.displayName + "!", ChatFormatting.LIGHT_PURPLE);
    }

    /** Anti-V: strips all powers (Compound V and cursed techniques alike). */
    public static Component drinkAntiV(ServerPlayer player) {
        DataManager.revokeAllPowers(player.getStringUUID());
        return msg(player, "Anti-V floods your system. Your powers go dark.", ChatFormatting.GRAY);
    }

    private static Component msg(ServerPlayer player, String text, ChatFormatting color) {
        Component c = Component.literal(text).withStyle(color);
        player.sendSystemMessage(c);
        return c;
    }
}
