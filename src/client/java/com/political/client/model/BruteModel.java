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
 * Hulking brute archetype: a broad, inflated humanoid with jagged shoulder spikes and over-sized
 * arms held in a heavy ape-like hunch. Adds a lumbering shoulder-roll on top of the humanoid rig.
 */
public class BruteModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart leftSpike;
    private final ModelPart rightSpike;

    public BruteModel(ModelPart root) {
        super(root);
        this.leftSpike = this.body.getChild("brute_left_spike");
        this.rightSpike = this.body.getChild("brute_right_spike");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.55f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // Jagged shoulder spikes erupting from the traps.
        body.addOrReplaceChild("brute_left_spike",
                CubeListBuilder.create().texOffs(0, 32).addBox(-1.5f, -3.0f, -1.5f, 3.0f, 4.0f, 3.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(-5.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.7f));
        body.addOrReplaceChild("brute_right_spike",
                CubeListBuilder.create().texOffs(0, 32).addBox(-1.5f, -3.0f, -1.5f, 3.0f, 4.0f, 3.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(5.0f, 1.0f, 0.0f, 0.0f, 0.0f, -0.7f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        // Permanent heavy hunch with arms splayed wide off the body.
        this.body.xRot += 0.1f;
        this.rightArm.zRot += 0.18f;
        this.leftArm.zRot -= 0.18f;
        // Slow side-to-side shoulder roll so the bulk feels weighty.
        float roll = Mth.sin(state.ageInTicks * 0.05f) * 0.05f;
        this.body.zRot = roll;
        this.head.zRot = -roll;
    }
}
