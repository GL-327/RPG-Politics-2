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
 * Demon / fiend archetype: a broad, hunched humanoid crowned with great curved horns, with folding
 * bat-wings on the back, a lashing tail and taloned hands. Wings twitch and flare, the tail sways,
 * and a swipe flings the wings wide on attack — devils, fiends, hellspawn, infernal curses.
 */
public class DemonModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail1;
    private final ModelPart tail2;

    public DemonModel(ModelPart root) {
        super(root);
        this.leftWing = this.body.getChild("demon_wing_left");
        this.rightWing = this.body.getChild("demon_wing_right");
        this.tail1 = this.body.getChild("demon_tail1");
        this.tail2 = this.tail1.getChild("demon_tail2");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.3f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // Great curved horns sweeping up and back.
        head.addOrReplaceChild("demon_horn_left",
                CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, -6.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(-3.5f, -6.5f, 0.0f, -0.3f, 0.0f, -0.7f));
        head.addOrReplaceChild("demon_horn_right",
                CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, -6.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(-0.2f)),
                PartPose.offsetAndRotation(3.5f, -6.5f, 0.0f, -0.3f, 0.0f, 0.7f));
        // Folding bat-wings (membrane planes) on the shoulder blades.
        body.addOrReplaceChild("demon_wing_left",
                CubeListBuilder.create().texOffs(0, 32).addBox(0.0f, 0.0f, 0.0f, 12.0f, 14.0f, 0.0f),
                PartPose.offsetAndRotation(2.0f, 1.0f, 2.5f, 0.0f, -0.7f, 0.0f));
        body.addOrReplaceChild("demon_wing_right",
                CubeListBuilder.create().texOffs(0, 32).addBox(-12.0f, 0.0f, 0.0f, 12.0f, 14.0f, 0.0f),
                PartPose.offsetAndRotation(-2.0f, 1.0f, 2.5f, 0.0f, 0.7f, 0.0f));
        // Two-segment lashing tail.
        PartDefinition tail1 = body.addOrReplaceChild("demon_tail1",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f, new CubeDeformation(-0.1f)),
                PartPose.offsetAndRotation(0.0f, 10.0f, 2.5f, 0.6f, 0.0f, 0.0f));
        tail1.addOrReplaceChild("demon_tail2",
                CubeListBuilder.create().texOffs(40, 16).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 6.0f, 1.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 6.5f, 0.0f, 0.5f, 0.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float atk = Anim.attack(state);
        boolean aggro = Anim.aggressive(state);

        // Aggressive forward hunch with arms held wide.
        this.body.xRot += 0.08f;
        this.rightArm.zRot += 0.22f;
        this.leftArm.zRot -= 0.22f;

        // Wings: idle twitch, flared wide while hunting and flung open on a swipe.
        float flare = (aggro ? 0.5f : 0.0f) + atk * 0.6f;
        float flap = Mth.sin(t * 0.15f) * 0.1f;
        this.leftWing.yRot = -0.7f - flare + flap;
        this.rightWing.yRot = 0.7f + flare - flap;

        // Tail sways constantly, whips on attack.
        this.tail1.yRot = Mth.sin(t * 0.12f) * 0.4f + atk * 0.6f;
        this.tail2.yRot = Mth.sin(t * 0.12f - 0.8f) * 0.5f;

        // Claw swipe.
        if (atk > 0f) {
            this.rightArm.xRot = -0.6f - Mth.sin(atk * (float) Math.PI) * 1.4f;
        }
    }
}
