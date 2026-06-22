package com.political.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cast-pose timer replacing NotEnoughAnimations for technique casting. When active,
 * {@link com.political.client.mixin.HumanoidModelCastPoseMixin} raises the player's arms into a
 * forward cursed-energy weaving pose.
 */
public final class TechniqueCastPose {

    /** Ticks to hold the cast pose after a technique fires. */
    public static final int CAST_POSE_DURATION = 24;

    private static final Map<UUID, Integer> POSE_TICKS = new ConcurrentHashMap<>();

    private TechniqueCastPose() {}

    public static void trigger(UUID playerUuid) {
        if (playerUuid != null) {
            POSE_TICKS.put(playerUuid, CAST_POSE_DURATION);
        }
    }

    public static void clientTick() {
        POSE_TICKS.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });
    }

    /**
     * Returns true when the render state belongs to a player who is currently in a cast pose.
     * MC 26.2 render states carry position + entity type but not entity id, so we match by UUID
     * and proximity to the interpolated render position.
     */
    public static boolean isActiveAt(EntityRenderState state) {
        if (state.entityType != EntityTypes.PLAYER || POSE_TICKS.isEmpty()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        for (var entry : POSE_TICKS.entrySet()) {
            if (entry.getValue() <= 0) continue;
            Player player = mc.level.getPlayerByUUID(entry.getKey());
            if (player == null) continue;
            if (player.distanceToSqr(state.x, state.y, state.z) < 0.35) {
                return true;
            }
        }
        return false;
    }

    /** Arm pitch (radians) for the casting pose — arms raised forward. */
    public static float armPitch() {
        return -1.35f;
    }
}
