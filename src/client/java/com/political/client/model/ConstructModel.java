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
 * Construct / golem archetype: a heavy, blocky automaton — an oversized square head, massive slab
 * arms, a thick chassis with a glowing core plate and short stout legs. Moves with a slow, stomping
 * gait and a power-core hum, and brings both arms down in a ground-shaking slam on attack — stone
 * golems, clockwork sentinels, animated statues.
 */
public class ConstructModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart core;
    private final float rightArmX0;
    private final float leftArmX0;

    public ConstructModel(ModelPart root) {
        super(root);
        this.core = this.body.getChild("construct_core");
        this.rightArmX0 = this.rightArm.x;
        this.leftArmX0 = this.leftArm.x;
    }

    public static LayerDefinition createLayer() {
        // Bulky chassis; arms inflated further below.
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.45f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // Heavy slab arms bolted over the rig arms.
        root.getChild("right_arm").addOrReplaceChild("construct_plate",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.5f, -3.0f, 6.0f, 14.0f, 6.0f, new CubeDeformation(0.6f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));
        root.getChild("left_arm").addOrReplaceChild("construct_plate",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.5f, -3.0f, 6.0f, 14.0f, 6.0f, new CubeDeformation(0.6f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));
        // A blocky brow visor across the head.
        head.addOrReplaceChild("construct_visor",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -3.0f, -5.0f, 9.0f, 2.0f, 1.0f, new CubeDeformation(0.3f)),
                PartPose.ZERO);
        // The exposed power core on the chest.
        body.addOrReplaceChild("construct_core",
                CubeListBuilder.create().texOffs(16, 16).addBox(-2.5f, -2.5f, -1.0f, 5.0f, 5.0f, 2.0f, new CubeDeformation(0.1f)),
                PartPose.offset(0.0f, 4.0f, -2.5f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        float atk = Anim.attack(state);

        // Wide, immovable stance; arms held out from the bulk.
        this.rightArm.x = rightArmX0 - 1.0f;
        this.leftArm.x = leftArmX0 + 1.0f;
        this.rightArm.zRot += 0.12f;
        this.leftArm.zRot -= 0.12f;

        // Idle core hum: tiny vertical jitter on the chest core.
        this.core.z = -2.5f + Mth.sin(t * 0.2f) * 0.05f;

        // Heavy, exaggerated stomp on top of the walk swing.
        float stomp = Mth.cos(state.walkAnimationPos * 0.6662f) * 0.6f * speed;
        this.rightLeg.xRot += stomp * 0.3f;
        this.leftLeg.xRot -= stomp * 0.3f;
        this.body.y += Math.abs(Mth.sin(state.walkAnimationPos * 0.6662f)) * speed * 0.5f;

        // Two-armed overhead slam on attack.
        if (atk > 0f) {
            float slam = Mth.sin(atk * (float) Math.PI);
            this.rightArm.xRot = -2.2f * slam;
            this.leftArm.xRot = -2.2f * slam;
        }
    }
}
