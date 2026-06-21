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
 * Crawler archetype: a low, hunkered six-limbed body — the four biped limbs are splayed out wide as
 * skittering legs and a third, mid pair sprouts from the flanks. Limbs twitch on a fast scuttle
 * cadence and the body bobs low to the ground — larvae, ticks, spider-curses, scuttling constructs.
 */
public class CrawlerModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart midRightLeg;
    private final ModelPart midLeftLeg;

    public CrawlerModel(ModelPart root) {
        super(root);
        this.midRightLeg = this.body.getChild("mid_right_leg");
        this.midLeftLeg = this.body.getChild("mid_left_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        body.addOrReplaceChild("mid_right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-8.0f, -1.0f, -1.0f, 8.0f, 2.0f, 2.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(-4.0f, 8.0f, 0.0f, 0.0f, 0.0f, 0.3f));
        body.addOrReplaceChild("mid_left_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(0.0f, -1.0f, -1.0f, 8.0f, 2.0f, 2.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(4.0f, 8.0f, 0.0f, 0.0f, 0.0f, -0.3f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        float scuttle = Mth.cos(pos * 1.1f) * 0.5f * (0.3f + speed);
        float scuttleAlt = Mth.cos(pos * 1.1f + (float) Math.PI) * 0.5f * (0.3f + speed);

        // Low, hunched torso.
        this.body.xRot = 1.1f;
        this.body.y = 9.0f;
        this.head.y = 9.0f;
        this.head.z = -4.0f;

        // Front legs (arms) splayed out and forward, skittering.
        this.rightArm.x = -4.0f; this.rightArm.y = 16.0f; this.rightArm.z = -3.0f;
        this.leftArm.x = 4.0f;  this.leftArm.y = 16.0f; this.leftArm.z = -3.0f;
        this.rightArm.zRot = 0.8f;  this.rightArm.xRot = -0.4f + scuttle;
        this.leftArm.zRot = -0.8f;  this.leftArm.xRot = -0.4f + scuttleAlt;

        // Back legs (legs) splayed out and back.
        this.rightLeg.x = -3.0f; this.rightLeg.y = 16.0f; this.rightLeg.z = 3.0f;
        this.leftLeg.x = 3.0f;  this.leftLeg.y = 16.0f; this.leftLeg.z = 3.0f;
        this.rightLeg.zRot = 0.6f; this.rightLeg.xRot = 0.3f + scuttleAlt;
        this.leftLeg.zRot = -0.6f; this.leftLeg.xRot = 0.3f + scuttle;

        // Mid legs twitch opposite the others.
        this.midRightLeg.zRot = 0.3f + scuttle * 0.6f;
        this.midLeftLeg.zRot = -0.3f - scuttle * 0.6f;
    }
}
