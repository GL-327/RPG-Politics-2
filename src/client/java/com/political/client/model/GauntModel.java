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
 * Gaunt humanoid archetype: a thin, slightly stooped silhouette with elongated clawed forearms.
 * Rides the humanoid rig (vanilla walk/attack/head-track for free) and adds a subtle breathing/idle
 * stoop plus claw flex so it always looks alive even when standing still.
 */
public class GauntModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart rightClaw;
    private final ModelPart leftClaw;

    public GauntModel(ModelPart root) {
        super(root);
        this.rightClaw = this.rightArm.getChild("gaunt_claw");
        this.leftClaw = this.leftArm.getChild("gaunt_claw");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(-0.1f), 0.0f);
        PartDefinition root = mesh.getRoot();

        // Long, splayed claw fingers hanging off each forearm (reuse arm-skin UVs so they tint).
        root.getChild("right_arm").addOrReplaceChild("gaunt_claw",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, 7.5f, -1.0f, 2.0f, 4.0f, 1.0f, new CubeDeformation(-0.2f)),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("gaunt_claw",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, 7.5f, -1.0f, 2.0f, 4.0f, 1.0f, new CubeDeformation(-0.2f)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float idle = Mth.sin(state.ageInTicks * 0.07f) * 0.06f;
        // Permanent hunch + slow breathing sway.
        this.body.xRot += 0.12f + idle;
        this.head.xRot -= 0.08f; // crane the head forward off the stoop
        // Claws flex/grasp on the idle cadence.
        float flex = (Mth.sin(state.ageInTicks * 0.18f) + 1.0f) * 0.18f;
        this.rightClaw.xRot = flex;
        this.leftClaw.xRot = flex;
    }
}
