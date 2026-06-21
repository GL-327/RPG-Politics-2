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
 * A bespoke quadruped deer model for the Meadow Stag: a horizontal torso, a forward neck/head with
 * a short muzzle and a pair of branched antlers, and four legs that trot in a diagonal gait. Built
 * fully from scratch (not the humanoid rig) on the 64x32 UV layout of {@code meadow_stag.png}.
 */
public class StagModel extends EntityModel<CreatureRenderState> {

    private final ModelPart head;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    public StagModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.legFrontLeft = root.getChild("leg_front_left");
        this.legFrontRight = root.getChild("leg_front_right");
        this.legBackLeft = root.getChild("leg_back_left");
        this.legBackRight = root.getChild("leg_back_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -6.0f, 6.0f, 6.0f, 12.0f),
                PartPose.offset(0.0f, 12.0f, 0.0f));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-2.0f, -3.0f, -4.0f, 4.0f, 5.0f, 4.0f)
                        .texOffs(16, 18).addBox(-1.5f, -1.0f, -7.0f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 6.0f, -6.0f));
        // Branched antlers in a free corner of the sheet.
        head.addOrReplaceChild("antler_left",
                CubeListBuilder.create().texOffs(36, 0).addBox(0.0f, -6.0f, -1.0f, 1.0f, 6.0f, 1.0f),
                PartPose.offsetAndRotation(2.0f, -3.0f, -2.0f, -0.3f, 0.0f, 0.4f));
        head.addOrReplaceChild("antler_right",
                CubeListBuilder.create().texOffs(42, 0).addBox(-1.0f, -6.0f, -1.0f, 1.0f, 6.0f, 1.0f),
                PartPose.offsetAndRotation(-2.0f, -3.0f, -2.0f, -0.3f, 0.0f, -0.4f));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 24).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f);
        root.addOrReplaceChild("leg_front_left", leg, PartPose.offset(2.0f, 16.0f, -4.0f));
        root.addOrReplaceChild("leg_front_right", leg, PartPose.offset(-2.0f, 16.0f, -4.0f));
        root.addOrReplaceChild("leg_back_left", leg, PartPose.offset(2.0f, 16.0f, 4.0f));
        root.addOrReplaceChild("leg_back_right", leg, PartPose.offset(-2.0f, 16.0f, 4.0f));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * ((float) Math.PI / 180f);
        this.head.yRot = state.yRot * ((float) Math.PI / 180f);

        float swing = Mth.cos(state.walkAnimationPos * 0.6f) * 1.2f * Math.min(1.0f, state.walkAnimationSpeed);
        this.legFrontLeft.xRot = swing;
        this.legBackRight.xRot = swing;
        this.legFrontRight.xRot = -swing;
        this.legBackLeft.xRot = -swing;
    }
}
