package com.political.curse.domain;

import com.political.curse.SorcererGrade;
import com.political.curse.energy.CursedEnergyManager;
import com.political.net.DomainSyncS2C;
import com.political.sound.VfxSounds;
import com.political.vfx.VfxHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks active domain expansions and drives them each server tick: a one-shot expansion flash on
 * cast, a cheap per-tick {@code domainPulse}, and the sure-hit {@link DomainEffect} applied on the
 * domain's interval to every entity inside. One domain per caster at a time.
 *
 * <p>{@link #tick(MinecraftServer)} is wired from {@code JjkBootstrap.init} via Fabric's
 * {@code END_SERVER_TICK} event — no mixins.</p>
 */
public final class DomainManager {

    private static final Map<UUID, Active> ACTIVE = new ConcurrentHashMap<>();

    private DomainManager() {}

    private static final class Active {
        final ServerPlayer caster;
        final CursedDomain domain;
        final Vec3 center;
        int age;
        int ticksLeft;
        double phase;

        Active(ServerPlayer caster, CursedDomain domain, Vec3 center) {
            this.caster = caster;
            this.domain = domain;
            this.center = center;
            this.ticksLeft = domain.durationTicks();
        }
    }

    /** Expands a specific domain (or, if {@code domainId} is blank, the player's best available). */
    public static Component expand(ServerPlayer player, String domainId) {
        if (!DomainRegistry.isReady()) return fail("Domains are not ready.");
        int grade = SorcererGrade.of(player);
        CursedDomain domain = (domainId == null || domainId.isBlank())
                ? DomainRegistry.bestFor(grade)
                : DomainRegistry.byId(domainId);
        if (domain == null) return fail("You have not mastered any domain yet.");
        if (!SorcererGrade.meets(grade, domain.requiredGrade())) {
            return fail("Your grade is too low to expand " + domain.displayName() + ".");
        }
        if (ACTIVE.containsKey(player.getUUID())) {
            return fail("Your domain is already open.");
        }
        if (!CursedEnergyManager.has(player, domain.ceCost())) {
            return fail("Not enough cursed energy to expand " + domain.displayName() + ".");
        }
        CursedEnergyManager.spend(player, domain.ceCost());

        ServerLevel level = player.level();
        Vec3 center = player.position();
        VfxHelper.domainExpansion(level, domain.element(), center.add(0, 1.0, 0), domain.radius());
        if (VfxSounds.VFX_DOMAIN_WALL != null) {
            VfxSounds.play(level, center.add(0, 1.0, 0), VfxSounds.VFX_DOMAIN_WALL, 1.4f, 0.75f);
        }
        Active active = new Active(player, domain, center);
        ACTIVE.put(player.getUUID(), active);
        broadcast(active, domain.durationTicks());
        return Component.literal("Domain Expansion \u2014 " + domain.displayName())
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
    }

    public static boolean isActive(UUID uuid) {
        return ACTIVE.containsKey(uuid);
    }

    public static void clear(UUID uuid) {
        ACTIVE.remove(uuid);
    }

    public static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;
        for (Iterator<Map.Entry<UUID, Active>> it = ACTIVE.entrySet().iterator(); it.hasNext(); ) {
            Active a = it.next().getValue();
            ServerPlayer caster = a.caster;
            if (caster.isRemoved() || !caster.isAlive() || !(caster.level() instanceof ServerLevel level)) {
                it.remove();
                continue;
            }
            a.age++;
            a.ticksLeft--;
            a.phase += 0.18;

            VfxHelper.domainPulse(level, a.domain.element(), a.center.add(0, 0.2, 0), a.domain.radius(), a.phase);

            if (a.age % Math.max(1, a.domain.applyInterval()) == 0) {
                AABB box = new AABB(a.center, a.center).inflate(a.domain.radius());
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != caster && e.isAlive() && e.distanceToSqr(a.center) <= a.domain.radius() * a.domain.radius())) {
                    if (victim instanceof ServerPlayer sp && (sp.isCreative() || sp.isSpectator())) continue;
                    try {
                        a.domain.effect().apply(level, caster, victim, a.age);
                    } catch (Exception ignored) {
                        // Never let a domain effect break the server tick.
                    }
                }
            }

            if (a.ticksLeft <= 0) {
                it.remove();
                broadcastClear(a.caster);
                caster.sendSystemMessage(Component.literal(a.domain.displayName() + " fades.")
                        .withStyle(ChatFormatting.DARK_GRAY), true);
            } else if (a.age % 10 == 0) {
                broadcast(a, a.ticksLeft);
            }
        }
    }

    private static void broadcast(Active a, int ticksLeft) {
        DomainSyncS2C payload = new DomainSyncS2C(
                a.caster.getId(),
                a.domain.id(),
                a.center.x,
                a.center.y,
                a.center.z,
                (float) a.domain.radius(),
                a.domain.element().ordinal(),
                ticksLeft);
        for (ServerPlayer viewer : a.caster.level().getServer().getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(viewer, payload);
        }
    }

    private static void broadcastClear(ServerPlayer caster) {
        DomainSyncS2C payload = new DomainSyncS2C(
                caster.getId(), "", 0, 0, 0, 0, 0, 0);
        for (ServerPlayer viewer : caster.level().getServer().getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(viewer, payload);
        }
    }

    private static Component fail(String message) {
        return Component.literal(message).withStyle(ChatFormatting.GRAY);
    }
}
