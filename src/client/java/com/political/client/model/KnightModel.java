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
 * Armoured knight archetype: a plated humanoid with a crested helmet, angular pauldrons, a thick
 * cuirass and a front/back tasset skirt that sways as it marches. Holds a stoic, weapon-ready stance
 * and delivers an overhead chop on attack — reads as a guard / paladin / man-at-arms rather than a
 * vanilla zombie.
 */
public class KnightModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart crest;
    private final ModelPart tassetFront;
    private final ModelPart tassetBack;

    public KnightModel(ModelPart root) {
        super(root);
        this.crest = this.head.getChild("knight_crest");
        this.tassetFront = this.body.getChild("knight_tasset_front");
        this.tassetBack = this.body.getChild("knight_tasset_back");
    }

    public static LayerDefinition createLayer() {
        // Slight plate bulk over the whole body.
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.25f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // Helmet shell + a tall plume crest.
        head.addOrReplaceChild("knight_helm",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -8.5f, -4.5f, 9.0f, 9.0f, 9.0f, new CubeDeformation(0.4f)),
                PartPose.ZERO);
        head.addOrReplaceChild("knight_crest",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5f, -5.0f, -1.0f, 1.0f, 5.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, -8.5f, 0.0f, 0.2f, 0.0f, 0.0f));
        // Angular pauldrons.
        body.addOrReplaceChild("knight_pauldron_l",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.5f, -2.0f, -2.5f, 5.0f, 4.0f, 5.0f, new CubeDeformation(0.35f)),
                PartPose.offsetAndRotation(-5.5f, 1.5f, 0.0f, 0.0f, 0.0f, 0.3f));
        body.addOrReplaceChild("knight_pauldron_r",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.5f, -2.0f, -2.5f, 5.0f, 4.0f, 5.0f, new CubeDeformation(0.35f)),
                PartPose.offsetAndRotation(5.5f, 1.5f, 0.0f, 0.0f, 0.0f, -0.3f));
        // Tasset skirt plates front and back.
        body.addOrReplaceChild("knight_tasset_front",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -0.5f, 8.0f, 7.0f, 1.0f, new CubeDeformation(0.2f)),
                PartPose.offset(0.0f, 11.0f, -2.2f));
        body.addOrReplaceChild("knight_tasset_back",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -0.5f, 8.0f, 7.0f, 1.0f, new CubeDeformation(0.2f)),
                PartPose.offset(0.0f, 11.0f, 2.2f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        float atk = Anim.attack(state);

        // Stoic upright posture; gentle armoured breathing.
        this.body.xRot += Mth.sin(t * 0.05f) * 0.02f;

        // Tassets swing opposite the legs as it marches.
        float skirt = Mth.cos(state.walkAnimationPos * 0.6662f) * 0.4f * speed;
        this.tassetFront.xRot = -skirt;
        this.tassetBack.xRot = skirt;

        // Overhead chop: raise the right arm, then drive it down across the swing.
        if (atk > 0f) {
            float raise = Mth.sin(atk * (float) Math.PI); // 0..1..0 over the swing
            this.rightArm.xRot = -2.4f * raise;
            this.rightArm.zRot = -0.15f;
            this.crest.xRot = 0.2f + raise * 0.2f;
        }
    }
}
