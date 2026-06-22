package com.political.client.mixin;

import com.political.client.animation.TechniqueCastPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Native player cast-pose hook (replaces NotEnoughAnimations dependency). When a player entity is
 * mid-technique cast, both arms are raised forward in a cursed-energy weaving stance.
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelCastPoseMixin {

    @Shadow
    public ModelPart rightArm;

    @Shadow
    public ModelPart leftArm;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void political$techniqueCastPose(HumanoidRenderState state, CallbackInfo ci) {
        if (!TechniqueCastPose.isActiveAt(state)) return;

        float pitch = TechniqueCastPose.armPitch();
        this.rightArm.xRot = pitch;
        this.leftArm.xRot = pitch;
        this.rightArm.yRot = -0.18f;
        this.leftArm.yRot = 0.18f;
        this.rightArm.zRot = -0.12f;
        this.leftArm.zRot = 0.12f;
    }
}
