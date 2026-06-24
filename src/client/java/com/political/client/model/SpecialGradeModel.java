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
 * Special-grade humanoid curse archetype: a tall, imposing near-human silhouette with a crest of
 * head-spikes, extra eyes set into a brow ridge, a marking plate on the chest and long clawed hands.
 * Stands in a menacing, slightly forward-leaning power pose and lunges hard on attack — the
 * disaster-curse / cursed-spirit special grade look (Jogo / Hanami / Mahito family).
 */
public class SpecialGradeModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart crestMid;
    private final ModelPart crestLeft;
    private final ModelPart crestRight;
    private final ModelPart rightClaw;
    private final ModelPart leftClaw;

    public SpecialGradeModel(ModelPart root) {
        super(root);
        this.crestMid = this.head.getChild("sg_crest_mid");
        this.crestLeft = this.head.getChild("sg_crest_left");
        this.crestRight = this.head.getChild("sg_crest_right");
        this.rightClaw = this.rightArm.getChild("sg_claw");
        this.leftClaw = this.leftArm.getChild("sg_claw");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.1f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // A heavy brow ridge with extra eyes (extra cube across the forehead).
        head.addOrReplaceChild("sg_brow",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -2.0f, -5.0f, 9.0f, 2.0f, 1.0f, new CubeDeformation(0.1f)),
                PartPose.offset(0.0f, -1.0f, 0.0f));
        // A swept-back crest of three blades.
        head.addOrReplaceChild("sg_crest_mid",
                CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, -8.0f, -0.5f, 2.0f, 8.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, -7.0f, 1.5f, -0.5f, 0.0f, 0.0f));
        head.addOrReplaceChild("sg_crest_left",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5f, -6.0f, -0.5f, 1.0f, 6.0f, 1.0f),
                PartPose.offsetAndRotation(-2.5f, -7.0f, 1.5f, -0.5f, 0.0f, -0.5f));
        head.addOrReplaceChild("sg_crest_right",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5f, -6.0f, -0.5f, 1.0f, 6.0f, 1.0f),
                PartPose.offsetAndRotation(2.5f, -7.0f, 1.5f, -0.5f, 0.0f, 0.5f));
        // A raised marking plate on the chest.
        body.addOrReplaceChild("sg_plate",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -0.5f, 8.0f, 7.0f, 1.0f, new CubeDeformation(0.2f)),
                PartPose.offset(0.0f, 1.0f, -2.0f));
        // Long clawed hands hanging off each forearm.
        root.getChild("right_arm").addOrReplaceChild("sg_claw",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 4.0f, 3.0f, new CubeDeformation(-0.4f)),
                PartPose.offset(0.0f, 10.0f, 0.0f));
        root.getChild("left_arm").addOrReplaceChild("sg_claw",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 4.0f, 3.0f, new CubeDeformation(-0.4f)),
                PartPose.offset(0.0f, 10.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float atk = Anim.attack(state);
        int phase = Anim.phase(state);

        // Imposing forward lean; arms held out from the body, claws splayed.
        this.body.xRot += 0.06f;
        this.rightArm.zRot += 0.2f;
        this.leftArm.zRot -= 0.2f;

        // Slow, predatory sway — quickens in the enraged phase.
        float swaySpeed = phase >= 2 ? 0.12f : 0.05f;
        float sway = Mth.sin(t * swaySpeed) * (0.05f + (phase >= 2 ? 0.05f : 0.0f));
        this.body.zRot = sway;
        this.head.zRot = -sway;
        this.head.xRot -= 0.05f;

        // Claw grasp idle + hard double-handed forward lunge on attack.
        float flex = (Mth.sin(t * 0.2f) + 1.0f) * 0.2f + atk;
        this.rightClaw.xRot = flex;
        this.leftClaw.xRot = flex;
        this.rightArm.xRot -= atk * 1.4f;
        this.leftArm.xRot -= atk * 1.4f;
        this.crestMid.xRot = -0.5f - atk * 0.4f;
        this.crestLeft.zRot = -0.5f - atk * 0.2f;
        this.crestRight.zRot = 0.5f + atk * 0.2f;
    }
}
