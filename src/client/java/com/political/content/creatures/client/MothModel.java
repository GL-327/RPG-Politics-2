package com.political.content.creatures.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A bespoke insectoid model for the Glimmermoth: a fuzzy body, a small head with feathered antennae,
 * and two broad wings that beat continuously. Built from scratch on the 32x32 UV layout of
 * {@code glimmermoth.png}.
 */
public class MothModel extends EntityModel<CreatureRenderState> {

    private final ModelPart wingLeft;
    private final ModelPart wingRight;

    public MothModel(ModelPart root) {
        super(root);
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1.5f, -2.5f, 3.0f, 3.0f, 6.0f),
                PartPose.offset(0.0f, 18.0f, 0.0f));
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 9).addBox(-1.5f, -1.5f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 18.0f, -3.0f));
        head.addOrReplaceChild("antenna_left",
                CubeListBuilder.create().texOffs(0, 16).addBox(0.0f, -3.0f, 0.0f, 1.0f, 3.0f, 0.0f),
                PartPose.offsetAndRotation(1.0f, -1.0f, -1.0f, -0.4f, 0.0f, 0.3f));
        head.addOrReplaceChild("antenna_right",
                CubeListBuilder.create().texOffs(4, 16).addBox(-1.0f, -3.0f, 0.0f, 1.0f, 3.0f, 0.0f),
                PartPose.offsetAndRotation(-1.0f, -1.0f, -1.0f, -0.4f, 0.0f, -0.3f));

        root.addOrReplaceChild("wing_left",
                CubeListBuilder.create().texOffs(12, 0).addBox(0.0f, -0.5f, -3.0f, 8.0f, 0.0f, 7.0f),
                PartPose.offset(1.0f, 17.0f, 0.0f));
        root.addOrReplaceChild("wing_right",
                CubeListBuilder.create().texOffs(12, 8).addBox(-8.0f, -0.5f, -3.0f, 8.0f, 0.0f, 7.0f),
                PartPose.offset(-1.0f, 17.0f, 0.0f));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        // Continuous wingbeat driven by age so it flutters even while hovering.
        float beat = Mth.cos(state.ageInTicks * 1.3f) * 0.8f + 0.4f;
        this.wingLeft.zRot = beat;
        this.wingRight.zRot = -beat;
    }
}
