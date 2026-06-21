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
 * Multi-armed archetype: a humanoid with a second, lower pair of arms sprouting from the ribs. The
 * lower arms swing on an offset cadence from the main arms (and counter-swing while attacking) so
 * the creature looks busy and many-handed — tool revenants, asura-curses, weaving constructs.
 */
public class MultiArmModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart lowerRightArm;
    private final ModelPart lowerLeftArm;

    public MultiArmModel(ModelPart root) {
        super(root);
        this.lowerRightArm = this.body.getChild("lower_right_arm");
        this.lowerLeftArm = this.body.getChild("lower_left_arm");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // Lower arms reuse the right-arm UV box so they tint like the upper arms.
        body.addOrReplaceChild("lower_right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 3.0f, 10.0f, 4.0f, new CubeDeformation(-0.3f)),
                PartPose.offset(-4.0f, 5.0f, 0.0f));
        body.addOrReplaceChild("lower_left_arm",
                CubeListBuilder.create().mirror().texOffs(40, 16).addBox(0.0f, -2.0f, -2.0f, 3.0f, 10.0f, 4.0f, new CubeDeformation(-0.3f)),
                PartPose.offset(4.0f, 5.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float swing = Mth.cos(state.walkAnimationPos * 0.6662f) * 1.0f * state.walkAnimationSpeed * 0.5f;
        float idle = Mth.sin(state.ageInTicks * 0.09f) * 0.12f;
        // Lower arms held outward, swinging opposite the legs with an extra idle flourish.
        this.lowerRightArm.xRot = swing + idle;
        this.lowerLeftArm.xRot = -swing + idle;
        this.lowerRightArm.zRot = 0.55f - state.attackTime * 0.4f;
        this.lowerLeftArm.zRot = -0.55f + state.attackTime * 0.4f;
    }
}
