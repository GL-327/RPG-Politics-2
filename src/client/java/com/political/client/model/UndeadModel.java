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
 * Undead husk archetype: an emaciated, skeletal frame — thin shrunken limbs, an exposed ribcage over
 * a sunken torso and a dropped jaw under the skull. Lurches in an uneven, broken-gait shamble with
 * twitching limbs and a lolling head, and reaches out with a grasping claw on attack — skeletons,
 * ghouls, wights, risen dead.
 */
public class UndeadModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart jaw;
    private final ModelPart ribLeft;
    private final ModelPart ribRight;

    public UndeadModel(ModelPart root) {
        super(root);
        this.jaw = this.head.getChild("undead_jaw");
        this.ribLeft = this.body.getChild("undead_rib_left");
        this.ribRight = this.body.getChild("undead_rib_right");
    }

    public static LayerDefinition createLayer() {
        // Withered, shrunken flesh on the limbs and torso.
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(-0.6f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // A dropped, gaunt jaw.
        head.addOrReplaceChild("undead_jaw",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, 0.0f, -4.0f, 6.0f, 2.0f, 4.0f, new CubeDeformation(-0.3f)),
                PartPose.offset(0.0f, 1.0f, 0.0f));
        // Exposed ribs arcing off each side of the spine.
        body.addOrReplaceChild("undead_rib_left",
                CubeListBuilder.create().texOffs(16, 16).addBox(0.0f, 0.0f, -2.5f, 1.0f, 8.0f, 5.0f, new CubeDeformation(-0.1f)),
                PartPose.offsetAndRotation(-3.2f, 1.0f, 0.0f, 0.0f, 0.0f, -0.15f));
        body.addOrReplaceChild("undead_rib_right",
                CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, 0.0f, -2.5f, 1.0f, 8.0f, 5.0f, new CubeDeformation(-0.1f)),
                PartPose.offsetAndRotation(3.2f, 1.0f, 0.0f, 0.0f, 0.0f, 0.15f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float atk = Anim.attack(state);

        // Permanent broken hunch and a lolling head.
        this.body.xRot += 0.18f;
        this.head.xRot += 0.1f + Mth.sin(t * 0.13f) * 0.08f;
        this.head.zRot = Mth.sin(t * 0.09f) * 0.12f;

        // Skeletal jaw chatters; ribs flex with each laboured breath.
        this.jaw.xRot = 0.15f + (Mth.sin(t * 0.3f) + 1.0f) * 0.1f;
        float breath = Mth.sin(t * 0.1f) * 0.05f;
        this.ribLeft.zRot = -0.15f - breath;
        this.ribRight.zRot = 0.15f + breath;

        // Uneven, twitchy shamble: arms drift up with offset timing.
        float twitch = Mth.sin(t * 0.25f) * 0.1f;
        this.rightArm.xRot += twitch - 0.2f;
        this.leftArm.xRot += -twitch * 0.7f - 0.15f;
        this.rightArm.zRot = 0.1f;
        this.leftArm.zRot = -0.1f;

        // Grasping reach on attack.
        if (atk > 0f) {
            this.rightArm.xRot = -1.3f * atk;
            this.leftArm.xRot = -1.1f * atk;
            this.jaw.xRot = 0.6f * atk + 0.15f;
        }
    }
}
