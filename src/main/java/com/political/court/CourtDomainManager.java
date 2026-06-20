package com.political.court;

import com.political.combat.CombatEngine;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Server-authoritative Court Domain (the JJK-style "domain expansion" for the judge).
 * Manages the arena bubble, boundary enforcement, the timed trial, and the Gavel's
 * Execution Strike. All visuals are server-driven (particle dome + action bar) so the
 * feature is robust on the new 26.2 client rendering pipeline.
 */
public final class CourtDomainManager {

    private static final double RADIUS = 12.0;
    private static final double HEIGHT = 8.0;
    private static final long DURATION_MS = 120_000L;
    private static final long MIN_TRIAL_MS = 3_000L;
    private static final long COOLDOWN_MS = 30_000L;

    private static volatile CourtSession active;
    private static long cooldownUntil = 0L;
    private static long lastBannerMs = 0L;
    private static int domeCounter = 0;

    private CourtDomainManager() {}

    public static boolean isActive() {
        return active != null;
    }

    public static void registerEvents() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer judge)) return InteractionResult.PASS;
            if (!(entity instanceof ServerPlayer target)) return InteractionResult.PASS;
            if (!CourtItems.isGavel(judge.getMainHandItem())) return InteractionResult.PASS;

            CourtSession s = active;
            if (s == null) {
                warn(judge, "Call court first with /court start <player>.");
                return InteractionResult.FAIL;
            }
            if (!s.judge.equals(judge.getUUID())) {
                warn(judge, "This is not your court.");
                return InteractionResult.FAIL;
            }
            if (!s.accused.equals(target.getUUID())) {
                warn(judge, "Only the accused may face the Gavel.");
                return InteractionResult.FAIL;
            }
            if (System.currentTimeMillis() < s.endTimeMillis - DURATION_MS + MIN_TRIAL_MS) {
                warn(judge, "The trial must be heard before sentencing.");
                return InteractionResult.FAIL;
            }

            CombatEngine.executeInstantKill(judge, target);
            adjourn("\u2696 The Gavel has fallen. " + target.getName().getString() + " has been executed by court order.");
            return InteractionResult.FAIL;
        });

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            CourtSession s = active;
            if (s == null || level.isClientSide()) return true;
            if (s.contains(Vec3.atCenterOf(pos))) {
                if (player instanceof ServerPlayer sp) {
                    warn(sp, "Blocks are protected inside a Court Domain.");
                }
                return false;
            }
            return true;
        });
    }

    public static boolean start(ServerPlayer judge, ServerPlayer accused) {
        long now = System.currentTimeMillis();
        if (active != null) {
            warn(judge, "A court is already in session.");
            return false;
        }
        if (now < cooldownUntil) {
            warn(judge, "The court is in recess for " + ((cooldownUntil - now) / 1000) + "s.");
            return false;
        }
        if (judge.getUUID().equals(accused.getUUID())) {
            warn(judge, "You cannot put yourself on trial.");
            return false;
        }

        ServerLevel level = judge.level();
        Vec3 center = judge.position();
        active = new CourtSession(judge.getUUID(), accused.getUUID(), level, center,
                RADIUS, HEIGHT, now + DURATION_MS);

        accused.connection.teleport(center.x, center.y, center.z, accused.getYRot(), accused.getXRot());
        if (!CourtItems.isGavel(judge.getMainHandItem()) && !judge.getInventory().add(CourtItems.createGavel())) {
            judge.drop(CourtItems.createGavel(), false);
        }

        MinecraftServer server = level.getServer();
        if (server != null) {
            broadcast(server, Component.literal("\u2696 COURT IS IN SESSION  \u2014  Judge "
                    + judge.getName().getString() + " presiding over " + accused.getName().getString())
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        level.playSound(null, center.x, center.y, center.z, SoundEvents.BELL_BLOCK, SoundSource.MASTER, 2.0f, 0.7f);
        return true;
    }

    public static void adjourn(String message) {
        CourtSession s = active;
        if (s == null) return;
        MinecraftServer server = s.level.getServer();
        active = null;
        cooldownUntil = System.currentTimeMillis() + COOLDOWN_MS;
        if (server != null) {
            broadcast(server, Component.literal(message).withStyle(ChatFormatting.YELLOW));
        }
    }

    public static void onPlayerRemoved(UUID uuid) {
        CourtSession s = active;
        if (s != null && (s.judge.equals(uuid) || s.accused.equals(uuid))) {
            adjourn("The court is adjourned: a key party has left.");
        }
    }

    public static void tick(MinecraftServer server) {
        CourtSession s = active;
        if (s == null) return;

        long now = System.currentTimeMillis();
        if (now >= s.endTimeMillis) {
            adjourn("\u2696 The court is adjourned. The accused walks free.");
            return;
        }

        ServerPlayer judge = server.getPlayerList().getPlayer(s.judge);
        ServerPlayer accused = server.getPlayerList().getPlayer(s.accused);
        if (judge == null || accused == null) {
            adjourn("The court is adjourned: a key party has left.");
            return;
        }

        if (domeCounter++ % 4 == 0) {
            drawDome(s);
        }
        enforceBounds(s, judge);
        enforceBounds(s, accused);

        if (now - lastBannerMs >= 1000) {
            lastBannerMs = now;
            Component banner = Component.literal("\u2696 COURT  \u2014  " + accused.getName().getString()
                    + " on trial  \u2014  " + s.remainingSeconds() + "s").withStyle(ChatFormatting.GOLD);
            judge.sendSystemMessage(banner, true);
            accused.sendSystemMessage(banner, true);
        }
    }

    private static void drawDome(CourtSession s) {
        int points = 36;
        for (double h = 0; h <= s.height; h += 2.0) {
            for (int i = 0; i < points; i++) {
                double ang = (Math.PI * 2 / points) * i;
                double x = s.center.x + Math.cos(ang) * s.radius;
                double z = s.center.z + Math.sin(ang) * s.radius;
                s.level.sendParticles(ParticleTypes.END_ROD, x, s.center.y + h, z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void enforceBounds(CourtSession s, ServerPlayer p) {
        Vec3 pos = p.position();
        if (s.contains(pos)) return;

        double dx = pos.x - s.center.x;
        double dz = pos.z - s.center.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        double clampX = pos.x, clampY = pos.y, clampZ = pos.z;

        if (dist > s.radius && dist > 0) {
            double scale = (s.radius - 0.5) / dist;
            clampX = s.center.x + dx * scale;
            clampZ = s.center.z + dz * scale;
        }
        if (pos.y > s.center.y + s.height) clampY = s.center.y + s.height - 0.5;
        if (pos.y < s.center.y - 2.0) clampY = s.center.y;

        p.connection.teleport(clampX, clampY, clampZ, p.getYRot(), p.getXRot());
        warn(p, "The Court Domain holds you within its bounds.");
    }

    private static void broadcast(MinecraftServer server, Component message) {
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            online.sendSystemMessage(message);
        }
    }

    private static void warn(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), true);
    }
}
