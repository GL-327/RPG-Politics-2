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
 * Cloaked spirit archetype: a hooded, horned curse whose legs are replaced by a flared, swaying robe
 * skirt; it drifts on a slow hover bob with sleeve-like arm sway. Reads as a spectral, floating
 * caster — wraiths, shades, hex-curses, hooded acolytes.
 */
public class CloakedModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart skirt;
    private final float headY0;
    private final float bodyY0;
    private final float rightArmY0;
    private final float leftArmY0;

    public CloakedModel(ModelPart root) {
        super(root);
        this.skirt = this.body.getChild("cloak_skirt");
        this.headY0 = this.head.y;
        this.bodyY0 = this.body.y;
        this.rightArmY0 = this.rightArm.y;
        this.leftArmY0 = this.leftArm.y;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // Pointed hood shell over the head.
        head.addOrReplaceChild("hood",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -8.5f, -4.5f, 9.0f, 9.0f, 9.0f, new CubeDeformation(0.6f)),
                PartPose.ZERO);
        // Curved horns sweeping back off the hood.
        head.addOrReplaceChild("left_horn",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5f, -5.0f, -0.5f, 1.0f, 5.0f, 1.0f),
                PartPose.offsetAndRotation(-3.5f, -7.5f, 0.0f, -0.4f, 0.0f, -0.6f));
        head.addOrReplaceChild("right_horn",
                CubeListBuilder.create().texOffs(60, 0).addBox(-0.5f, -5.0f, -0.5f, 1.0f, 5.0f, 1.0f),
                PartPose.offsetAndRotation(3.5f, -7.5f, 0.0f, -0.4f, 0.0f, 0.6f));
        // Flared robe skirt in place of legs.
        body.addOrReplaceChild("cloak_skirt",
                CubeListBuilder.create().texOffs(0, 32).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 14.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 11.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;

        // Floating — no walking legs.
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;

        float bob = Mth.sin(t * 0.08f) * 1.2f;
        this.head.y = headY0 + bob;
        this.body.y = bodyY0 + bob;
        this.rightArm.y = rightArmY0 + bob;
        this.leftArm.y = leftArmY0 + bob;

        // Sleeves held slightly out, drifting.
        float drift = Mth.sin(t * 0.06f) * 0.1f;
        this.rightArm.zRot = 0.25f + drift;
        this.leftArm.zRot = -0.25f - drift;

        // Robe hem sways like cloth.
        this.skirt.xRot = Mth.sin(t * 0.07f) * 0.08f;
        this.skirt.zRot = Mth.cos(t * 0.05f) * 0.06f;
    }
}
