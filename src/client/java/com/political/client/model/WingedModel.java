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
 * Winged archetype: a humanoid with a large pair of folding membrane wings on its back. The wings
 * idle-flutter when still and beat hard while moving; the body lifts on a gentle hover bob synced to
 * the wing-beat so airborne curses (owls, eagles, dragons, seraphs) feel lighter than foot-soldiers.
 */
public class WingedModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final float headY0;
    private final float bodyY0;

    public WingedModel(ModelPart root) {
        super(root);
        this.rightWing = this.body.getChild("right_wing");
        this.leftWing = this.body.getChild("left_wing");
        this.headY0 = this.head.y;
        this.bodyY0 = this.body.y;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // Thin membrane wings rooted at the shoulder blades, sweeping out and back.
        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 32).addBox(-10.0f, 0.0f, 0.0f, 10.0f, 16.0f, 0.0f),
                PartPose.offsetAndRotation(-1.0f, 0.0f, 2.5f, 0.0f, 0.4f, 0.0f));
        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 32).addBox(0.0f, 0.0f, 0.0f, 10.0f, 16.0f, 0.0f),
                PartPose.offsetAndRotation(1.0f, 0.0f, 2.5f, 0.0f, -0.4f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        // Faster, deeper beats while travelling; lazy flutter at rest.
        float beatSpeed = 0.3f + state.walkAnimationSpeed * 0.6f;
        float beat = Mth.cos(t * beatSpeed) * (0.35f + state.walkAnimationSpeed * 0.5f);
        this.rightWing.yRot = 0.4f + beat;
        this.leftWing.yRot = -0.4f - beat;
        // Hover bob, peaking with the downstroke.
        float bob = Mth.sin(t * beatSpeed) * 0.8f;
        this.head.y = headY0 + bob;
        this.body.y = bodyY0 + bob;
    }
}
