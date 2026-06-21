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
 * Tiny swarm archetype: a shrunken body with stubby limbs under a proportionally over-sized head
 * topped with twitching antennae. Everything jitters fast and the body hops on a quick cadence, so
 * fodder motes / fleas / gnats feel like restless little pests (final size comes from the SCALE
 * attribute + renderer shadow).
 */
public class SwarmModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart leftAntenna;
    private final ModelPart rightAntenna;
    private final float bodyY0;

    public SwarmModel(ModelPart root) {
        super(root);
        this.rightAntenna = this.head.getChild("right_antenna");
        this.leftAntenna = this.head.getChild("left_antenna");
        this.bodyY0 = this.body.y;
    }

    public static LayerDefinition createLayer() {
        // Shrunken body/limbs make the standard 8-cube head read as over-sized.
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(-0.35f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");

        head.addOrReplaceChild("right_antenna",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5f, -5.0f, -0.5f, 1.0f, 5.0f, 1.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(-2.0f, -8.0f, 0.0f, -0.3f, 0.0f, -0.3f));
        head.addOrReplaceChild("left_antenna",
                CubeListBuilder.create().texOffs(60, 0).addBox(-0.5f, -5.0f, -0.5f, 1.0f, 5.0f, 1.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(2.0f, -8.0f, 0.0f, -0.3f, 0.0f, 0.3f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        // Quick hop.
        float hop = Math.abs(Mth.sin(t * 0.4f)) * 1.0f;
        this.body.y = bodyY0 - hop;
        // Fast nervous limb jitter on top of the walk pose.
        float jitter = Mth.cos(t * 0.6f) * 0.25f;
        this.rightArm.zRot = 0.4f + jitter;
        this.leftArm.zRot = -0.4f - jitter;
        // Antennae wobble.
        float wob = Mth.sin(t * 0.5f) * 0.4f;
        this.rightAntenna.xRot = -0.3f + wob;
        this.leftAntenna.xRot = -0.3f - wob;
    }
}
