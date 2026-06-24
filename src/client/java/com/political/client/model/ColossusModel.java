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
 * Boss-scale colossus archetype: a massively inflated humanoid crowned with a ring of horns and
 * capped with heavy shoulder pauldrons. It moves with slow, weighty sway and a wide power stance so
 * bosses read as imposing even before the additive {@link GlowOverlayLayer} is applied on top.
 */
public class ColossusModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart leftPauldron;
    private final ModelPart rightPauldron;

    public ColossusModel(ModelPart root) {
        super(root);
        this.leftPauldron = this.body.getChild("colossus_left_pauldron");
        this.rightPauldron = this.body.getChild("colossus_right_pauldron");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.7f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // A crown of five horns.
        for (int i = 0; i < 5; i++) {
            float ang = (i - 2) * 0.45f;
            head.addOrReplaceChild("crown_horn_" + i,
                    CubeListBuilder.create().texOffs(56, 0).addBox(-0.5f, -5.0f, -0.5f, 1.0f, 5.0f, 1.0f),
                    PartPose.offsetAndRotation(Mth.sin(ang) * 4.0f, -8.0f, Mth.cos(ang) * 1.5f - 1.5f, -0.2f, 0.0f, ang));
        }
        // Heavy pauldrons.
        body.addOrReplaceChild("colossus_left_pauldron",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.5f, -2.5f, -2.5f, 5.0f, 5.0f, 5.0f, new CubeDeformation(0.3f)),
                PartPose.offsetAndRotation(-6.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.4f));
        body.addOrReplaceChild("colossus_right_pauldron",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.5f, -2.5f, -2.5f, 5.0f, 5.0f, 5.0f, new CubeDeformation(0.3f)),
                PartPose.offsetAndRotation(6.5f, 1.0f, 0.0f, 0.0f, 0.0f, -0.4f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float atk = Anim.attack(state);
        int phase = Anim.phase(state);
        boolean enraged = phase >= 2;

        // Wide, heavy power stance with arms held away from the bulk; wider and hunched when enraged.
        this.rightArm.zRot += 0.28f + (enraged ? 0.12f : 0.0f);
        this.leftArm.zRot -= 0.28f + (enraged ? 0.12f : 0.0f);
        if (enraged) this.body.xRot += 0.12f;

        // Slow, ponderous sway — faster and heavier in the enraged phase.
        float speed = enraged ? 0.09f : 0.04f;
        float amp = enraged ? 0.09f : 0.05f;
        float sway = Mth.sin(state.ageInTicks * speed) * amp;
        this.body.zRot = sway;
        this.head.zRot = -sway * 0.5f;
        this.leftPauldron.zRot = 0.4f + sway;
        this.rightPauldron.zRot = -0.4f + sway;

        // Heavy two-fisted overhead smash on attack.
        if (atk > 0f) {
            float smash = Mth.sin(atk * (float) Math.PI);
            this.rightArm.xRot = -2.0f * smash;
            this.leftArm.xRot = -2.0f * smash;
        }
    }
}
