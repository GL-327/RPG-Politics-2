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
 * Serpentine / floating archetype: a hovering upper torso trailing a long, three-segment tail in
 * place of legs, with the arms drawn in as small fins. The whole body bobs and the tail ripples in
 * a travelling sine wave so it reads as a weightless, swimming curse.
 */
public class SerpentModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final float headY0;
    private final float bodyY0;
    private final float rightArmY0;
    private final float leftArmY0;

    public SerpentModel(ModelPart root) {
        super(root);
        this.tail1 = this.body.getChild("serpent_tail1");
        this.tail2 = this.tail1.getChild("serpent_tail2");
        this.tail3 = this.tail2.getChild("serpent_tail3");
        this.headY0 = this.head.y;
        this.bodyY0 = this.body.y;
        this.rightArmY0 = this.rightArm.y;
        this.leftArmY0 = this.leftArm.y;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        PartDefinition tail1 = body.addOrReplaceChild("serpent_tail1",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f),
                PartPose.offset(0.0f, 11.0f, 0.0f));
        PartDefinition tail2 = tail1.addOrReplaceChild("serpent_tail2",
                CubeListBuilder.create().texOffs(16, 32).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 6.0f, 3.0f),
                PartPose.offset(0.0f, 5.5f, 0.0f));
        tail2.addOrReplaceChild("serpent_tail3",
                CubeListBuilder.create().texOffs(28, 32).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 6.0f, 2.0f),
                PartPose.offset(0.0f, 5.5f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;

        // No legs — they are replaced by the tail.
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;

        // Hover bob for the whole upper body.
        float bob = Mth.sin(t * 0.1f) * 1.5f;
        this.head.y = headY0 + bob;
        this.body.y = bodyY0 + bob;
        this.rightArm.y = rightArmY0 + bob;
        this.leftArm.y = leftArmY0 + bob;

        // Arms tucked in as little fins.
        this.rightArm.xRot = 0.4f;
        this.leftArm.xRot = 0.4f;
        this.rightArm.zRot = 0.9f;
        this.leftArm.zRot = -0.9f;

        // Travelling sine wave down the tail.
        this.tail1.zRot = Mth.cos(t * 0.15f) * 0.25f;
        this.tail2.zRot = Mth.cos(t * 0.15f - 0.9f) * 0.32f;
        this.tail3.zRot = Mth.cos(t * 0.15f - 1.8f) * 0.4f;
    }
}
