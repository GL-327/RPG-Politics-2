package com.political.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

/**
 * Quadruped beast archetype: the biped rig is re-posed at runtime into a four-legged stance — the
 * torso lies horizontal, the arms become front legs and the legs become hind legs, with the head
 * carried forward on a low neck and a snout. Legs move in a diagonal trot gait. Built from the
 * standard humanoid mesh (re-positioned each frame) so it stays robust and mixin-free.
 */
public class QuadrupedModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    public QuadrupedModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = mesh.getRoot();
        // A blunt snout so the head reads as an animal muzzle.
        root.getChild("head").addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -2.0f, -4.0f, 4.0f, 3.0f, 4.0f, new CubeDeformation(-0.2f)),
                PartPose.offset(0.0f, -1.0f, -4.0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        float breathe = Mth.sin(state.ageInTicks * 0.1f) * 0.03f;

        // Horizontal spine.
        this.body.xRot = 1.45f + breathe;
        this.body.y = 5.0f;
        this.body.z = 0.0f;

        // Head forward on a low neck, free to track the target.
        this.head.x = 0.0f;
        this.head.y = 6.0f;
        this.head.z = -7.0f;
        this.head.xRot = state.xRot * ((float) Math.PI / 180f) + 0.25f;
        this.head.yRot = state.yRot * ((float) Math.PI / 180f);
        this.head.zRot = 0.0f;

        // Re-seat limbs as four legs: arms forward, legs back.
        place(this.rightArm, -3.0f, 13.0f, -5.0f);
        place(this.leftArm, 3.0f, 13.0f, -5.0f);
        place(this.rightLeg, -3.0f, 13.0f, 5.0f);
        place(this.leftLeg, 3.0f, 13.0f, 5.0f);

        // Diagonal trot: front-right + back-left, then front-left + back-right.
        float amp = 1.3f * speed;
        float a = Mth.cos(pos * 0.6662f) * amp;
        float b = Mth.cos(pos * 0.6662f + (float) Math.PI) * amp;
        this.rightArm.xRot = a;
        this.leftLeg.xRot = a;
        this.leftArm.xRot = b;
        this.rightLeg.xRot = b;
        this.rightArm.zRot = 0.0f;
        this.leftArm.zRot = 0.0f;
    }

    private static void place(ModelPart part, float x, float y, float z) {
        part.x = x;
        part.y = y;
        part.z = z;
    }
}
