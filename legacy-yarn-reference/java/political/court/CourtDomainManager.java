package com.political.court;

import com.political.CustomItemHandler;
import com.political.DataManager;
import com.political.combat.CombatEngine;
import com.political.net.CourtEndS2C;
import com.political.net.CourtStartS2C;
import com.political.net.CourtTimerS2C;
import com.political.net.ModNetworking;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Judge's "Court Domain" - a Hiromi Higuruma inspired arena. The judge calls
 * court on an accused player; a particle dome traps both inside while the trial
 * runs. Striking the accused with The Gavel inside the domain is a guaranteed
 * execution that confiscates their gear.
 */
public final class CourtDomainManager {

    public static final double RADIUS = 15.0;
    public static final double HEIGHT = 20.0;
    public static final int DURATION_SECONDS = 90;
    private static final long DURATION_MS = DURATION_SECONDS * 1000L;
    private static final long COOLDOWN_MS = 5 * 60 * 1000L;
    private static final long MIN_TRIAL_MS = 5000L;

    private static volatile CourtSession active;
    private static int lastBroadcastSecond = -1;
    private static final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private CourtDomainManager() {}

    public static void registerEvents() {
        // The Gavel execution strike inside the domain.
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            CourtSession session = active;
            if (session == null) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity judge)) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;
            if (!judge.getUuid().equals(session.judge) || !target.getUuid().equals(session.accused)) {
                return ActionResult.PASS;
            }
            if (!CustomItemHandler.isTheGavel(judge.getMainHandStack())) {
                return ActionResult.PASS;
            }
            long elapsed = System.currentTimeMillis() - (session.endTimeMillis - DURATION_MS);
            if (elapsed < MIN_TRIAL_MS) {
                judge.sendMessage(Text.literal("The trial must run for a few seconds before sentencing.")
                        .formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
            CombatEngine.executeInstantKill(judge, target);
            end("EXECUTED");
            return ActionResult.FAIL;
        });

        // No block breaking inside an active domain.
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            CourtSession session = active;
            if (session == null) return true;
            return !session.contains(Vec3d.ofCenter(pos));
        });
    }

    public static boolean isActive() {
        return active != null;
    }

    public static long cooldownRemaining(UUID judge) {
        Long next = cooldowns.get(judge);
        if (next == null) return 0;
        return Math.max(0, next - System.currentTimeMillis());
    }

    /** Opens a court domain. Returns null on success, or an error message. */
    public static String start(ServerPlayerEntity judge, ServerPlayerEntity accused) {
        if (active != null) {
            return "A court is already in session.";
        }
        if (judge.getUuid().equals(accused.getUuid())) {
            return "You cannot put yourself on trial.";
        }
        String chair = DataManager.getChair();
        String judgeRole = DataManager.getJudge();
        if (accused.getUuidAsString().equals(chair) || accused.getUuidAsString().equals(judgeRole)) {
            return "The Chair and the Judge are immune to the court.";
        }
        long cd = cooldownRemaining(judge.getUuid());
        if (cd > 0) {
            return "The court is in recess for another " + (cd / 1000) + "s.";
        }

        ServerWorld world = judge.getServerWorld();
        Vec3d center = judge.getPos();
        long endTime = System.currentTimeMillis() + DURATION_MS;
        active = new CourtSession(judge.getUuid(), accused.getUuid(), world, center, RADIUS, HEIGHT, endTime);
        lastBroadcastSecond = -1;

        // Pull the accused into the dome next to the judge.
        accused.requestTeleport(center.x + 2.0, center.y, center.z);
        world.playSound(null, center.x, center.y, center.z,
                SoundEvents.BLOCK_BELL_USE, SoundCategory.MASTER, 1.5f, 0.6f);

        Text banner = Text.literal("\u2696 COURT IS IN SESSION \u2696").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD);
        for (ServerPlayerEntity online : judge.getServer().getPlayerManager().getPlayerList()) {
            online.sendMessage(banner, false);
            online.sendMessage(Text.literal(judge.getName().getString())
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(" has called ").formatted(Formatting.GRAY))
                    .append(Text.literal(accused.getName().getString()).formatted(Formatting.RED))
                    .append(Text.literal(" to trial.").formatted(Formatting.GRAY)), false);
            ModNetworking.send(online, new CourtStartS2C(
                    judge.getName().getString(), accused.getName().getString(), DURATION_SECONDS));
        }
        return null;
    }

    public static void tick(MinecraftServer server) {
        CourtSession session = active;
        if (session == null) return;

        if (session.remainingSeconds() <= 0) {
            end("ADJOURNED");
            return;
        }

        renderDome(session);
        enforceBounds(server, session);

        int remaining = session.remainingSeconds();
        if (remaining != lastBroadcastSecond) {
            lastBroadcastSecond = remaining;
            for (ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
                ModNetworking.send(online, new CourtTimerS2C(remaining));
            }
        }
    }

    private static void renderDome(CourtSession session) {
        ServerWorld world = session.world;
        Vec3d c = session.center;
        int points = 30;
        double time = (System.currentTimeMillis() % 4000) / 4000.0;
        for (double h = 0; h <= session.height; h += 4.0) {
            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2 * i / points) + time * Math.PI * 2;
                double x = c.x + Math.cos(angle) * session.radius;
                double z = c.z + Math.sin(angle) * session.radius;
                world.spawnParticles(ParticleTypes.END_ROD, x, c.y + h, z, 1, 0, 0, 0, 0.0);
            }
        }
    }

    private static void enforceBounds(MinecraftServer server, CourtSession session) {
        keepInside(server.getPlayerManager().getPlayer(session.judge), session);
        keepInside(server.getPlayerManager().getPlayer(session.accused), session);
    }

    private static void keepInside(ServerPlayerEntity player, CourtSession session) {
        if (player == null) return;
        Vec3d pos = player.getPos();
        double dx = pos.x - session.center.x;
        double dz = pos.z - session.center.z;
        double distSq = dx * dx + dz * dz;
        if (distSq > session.radius * session.radius) {
            Vec3d inward = new Vec3d(-dx, 0, -dz).normalize().multiply(0.6);
            player.setVelocity(inward.x, player.getVelocity().y, inward.z);
            player.velocityModified = true;
            player.sendMessage(Text.literal("You cannot leave the court.").formatted(Formatting.RED), true);
        }
    }

    public static void end(String verdict) {
        CourtSession session = active;
        if (session == null) return;
        active = null;
        cooldowns.put(session.judge, System.currentTimeMillis() + COOLDOWN_MS);

        MinecraftServer server = session.world.getServer();
        Text msg = Text.literal("\u2696 Court adjourned (" + verdict + ").")
                .formatted(Formatting.LIGHT_PURPLE);
        for (ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
            online.sendMessage(msg, false);
            ModNetworking.send(online, CourtEndS2C.INSTANCE);
        }
    }

    /** Ends any session that involves the given player (e.g. on disconnect/death). */
    public static void onPlayerRemoved(UUID uuid) {
        CourtSession session = active;
        if (session != null && (session.judge.equals(uuid) || session.accused.equals(uuid))) {
            end("ADJOURNED");
        }
    }
}
