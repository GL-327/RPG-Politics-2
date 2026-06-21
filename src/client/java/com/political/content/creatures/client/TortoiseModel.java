package com.political.content.creatures.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A bespoke shelled-reptile model for the Ridgeback Tortoise: a domed ridged carapace over a low
 * body, a stubby retractable head and four squat legs with a slow, heavy plodding gait. Built from
 * scratch on the 64x32 UV layout of {@code ridgeback_tortoise.png}.
 */
public class TortoiseModel extends EntityModel<CreatureRenderState> {

    private final ModelPart head;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    public TortoiseModel(ModelPart root) {
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
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -2.0f, -6.0f, 10.0f, 4.0f, 12.0f),
                PartPose.offset(0.0f, 20.0f, 0.0f));
        // Domed ridged shell.
        root.addOrReplaceChild("shell",
                CubeListBuilder.create().texOffs(0, 16).addBox(-4.5f, -4.0f, -5.5f, 9.0f, 5.0f, 11.0f, new CubeDeformation(0.2f)),
                PartPose.offset(0.0f, 18.0f, 0.0f));
        root.addOrReplaceChild("ridge",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, -6.0f, -5.0f, 2.0f, 2.0f, 10.0f),
                PartPose.offset(0.0f, 18.0f, 0.0f));

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -2.0f, -4.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 20.0f, -6.0f));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(44, 0).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 4.0f, 3.0f);
        root.addOrReplaceChild("leg_front_left", leg, PartPose.offset(3.5f, 20.0f, -4.0f));
        root.addOrReplaceChild("leg_front_right", leg, PartPose.offset(-3.5f, 20.0f, -4.0f));
        root.addOrReplaceChild("leg_back_left", leg, PartPose.offset(3.5f, 20.0f, 4.0f));
        root.addOrReplaceChild("leg_back_right", leg, PartPose.offset(-3.5f, 20.0f, 4.0f));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        this.head.yRot = state.yRot * ((float) Math.PI / 180f) * 0.5f;

        float swing = Mth.cos(state.walkAnimationPos * 0.4f) * 0.7f * Math.min(1.0f, state.walkAnimationSpeed);
        this.legFrontLeft.xRot = swing;
        this.legBackRight.xRot = swing;
        this.legFrontRight.xRot = -swing;
        this.legBackLeft.xRot = -swing;
    }
}
