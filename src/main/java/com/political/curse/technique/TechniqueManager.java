package com.political.curse.technique;

import com.political.curse.SorcererGrade;
import com.political.curse.domain.CursedDomain;
import com.political.curse.domain.DomainRegistry;
import com.political.curse.energy.CursedEnergyManager;
import com.political.net.TechniqueCastS2C;
import com.political.net.TechniqueMenuS2C;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import com.political.sound.VfxSounds;
import com.political.vfx.VfxHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Server-side entry point for casting cursed techniques and opening the technique screen.
 *
 * <p>Knowledge is derived purely from a player's sorcerer grade (a technique is "known" once the
 * player meets its {@code requiredGrade}), so nothing extra needs persisting. Per-technique cooldowns
 * are tracked in memory against the level game-time.</p>
 */
public final class TechniqueManager {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new ConcurrentHashMap<>();

    private TechniqueManager() {}

    /**
     * Attempts to cast {@code techniqueId} for {@code player}. Returns an action-bar feedback component
     * (success or the reason it failed). Never throws.
     */
    public static Component cast(ServerPlayer player, String techniqueId) {
        CursedTechnique t = TechniqueRegistry.byId(techniqueId);
        if (t == null) return fail("Unknown technique.");

        int grade = SorcererGrade.of(player);
        if (!SorcererGrade.meets(grade, t.requiredGrade())) {
            return fail("You are not yet skilled enough to weave " + t.displayName() + ".");
        }

        long now = player.level().getGameTime();
        Map<String, Long> mine = COOLDOWNS.computeIfAbsent(player.getUUID(), u -> new ConcurrentHashMap<>());
        Long readyAt = mine.get(techniqueId);
        if (readyAt != null && now < readyAt) {
            long secs = Math.max(1, (readyAt - now) / 20);
            return fail(t.displayName() + " is recharging (" + secs + "s).");
        }

        if (!CursedEnergyManager.has(player, t.ceCost())) {
            return fail("Not enough cursed energy for " + t.displayName() + ".");
        }

        boolean fired;
        try {
            fired = t.resolve(new TechniqueContext(player));
        } catch (Exception e) {
            return fail("The technique unravelled.");
        }
        if (!fired) return fail("No valid target for " + t.displayName() + ".");

        CursedEnergyManager.spend(player, t.ceCost());
        mine.put(techniqueId, now + t.cooldownTicks());
        VfxHelper.elementBurst(player.level(), t.element(), player.position().add(0, 1, 0), 0.7);
        playCastSound(player, t);
        broadcastCastPose(player);

        return Component.literal("\u2620 " + t.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    /** Builds and sends a fresh {@link TechniqueMenuS2C} to the player. */
    public static void openMenu(ServerPlayer player) {
        int grade = SorcererGrade.of(player);
        String known = TechniqueRegistry.knownFor(grade).stream()
                .map(CursedTechnique::id).collect(Collectors.joining(","));
        String domains = DomainRegistry.knownFor(grade).stream()
                .map(CursedDomain::id).collect(Collectors.joining(","));
        ServerPlayNetworking.send(player, new TechniqueMenuS2C(
                grade,
                (float) CursedEnergyManager.current(player),
                (float) CursedEnergyManager.max(player),
                known,
                "", // slot bindings are kept client-side
                domains));
    }

    public static void clear(UUID uuid) {
        COOLDOWNS.remove(uuid);
    }

    private static Component fail(String message) {
        return Component.literal(message).withStyle(ChatFormatting.GRAY);
    }

    private static void broadcastCastPose(ServerPlayer player) {
        TechniqueCastS2C payload = new TechniqueCastS2C(player.getUUID());
        for (ServerPlayer viewer : PlayerLookup.all(player.level().getServer())) {
            ServerPlayNetworking.send(viewer, payload);
        }
    }

    private static void playCastSound(ServerPlayer player, CursedTechnique t) {
        if (VfxSounds.VFX_SLASH == null) return;
        switch (t.element()) {
            case BLOOD, FIRE -> VfxSounds.play(player.level(), player.position().add(0, 1, 0),
                    VfxSounds.VFX_SLASH, 0.85f, 1.0f + player.getRandom().nextFloat() * 0.15f);
            case LIGHTNING -> {
                if (VfxSounds.VFX_CHAIN_LIGHTNING != null) {
                    VfxSounds.play(player.level(), player.position().add(0, 1, 0),
                            VfxSounds.VFX_CHAIN_LIGHTNING, 0.7f, 1.2f);
                }
            }
            case HOLY, ARCANE -> {
                if (VfxSounds.VFX_RUNE_HUM != null) {
                    VfxSounds.play(player.level(), player.position().add(0, 1, 0),
                            VfxSounds.VFX_RUNE_HUM, 0.6f, 1.0f);
                }
            }
            default -> { }
        }
    }
}
