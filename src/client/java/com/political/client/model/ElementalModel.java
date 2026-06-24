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
 * Elemental archetype: a floating core torso ringed by jagged orbiting shards, with wisp-arms and no
 * legs. The core hovers and pulses, the shard ring slowly counter-rotates and pulses outward, and the
 * shards flare wide on attack — flame / frost / storm / earth elementals and calamity lineages.
 */
public class ElementalModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart shardRing;
    private final float bodyY0;
    private final float headY0;
    private final float rightArmY0;
    private final float leftArmY0;

    public ElementalModel(ModelPart root) {
        super(root);
        this.shardRing = this.body.getChild("elem_ring");
        this.bodyY0 = this.body.y;
        this.headY0 = this.head.y;
        this.rightArmY0 = this.rightArm.y;
        this.leftArmY0 = this.leftArm.y;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(-0.2f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // A ring of jagged shards orbiting the core, built as one rotatable group.
        PartDefinition ring = body.addOrReplaceChild("elem_ring", CubeListBuilder.create(),
                PartPose.offset(0.0f, 4.0f, 0.0f));
        int shards = 6;
        for (int i = 0; i < shards; i++) {
            float ang = (float) (i * (Math.PI * 2.0 / shards));
            float x = Mth.cos(ang) * 6.0f;
            float z = Mth.sin(ang) * 6.0f;
            ring.addOrReplaceChild("shard_" + i,
                    CubeListBuilder.create().texOffs(0, 32).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(-0.2f)),
                    PartPose.offsetAndRotation(x, 0.0f, z, 0.0f, -ang, ang * 0.2f));
        }

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float atk = Anim.attack(state);

        // Floating — no legs.
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;

        // Hover bob for the whole core.
        float bob = Mth.sin(t * 0.1f) * 1.4f;
        this.body.y = bodyY0 + bob;
        this.head.y = headY0 + bob;
        this.rightArm.y = rightArmY0 + bob;
        this.leftArm.y = leftArmY0 + bob;

        // Wisp-arms held outward, drifting.
        float drift = Mth.sin(t * 0.13f) * 0.15f;
        this.rightArm.xRot = -0.3f + drift;
        this.leftArm.xRot = -0.3f - drift;
        this.rightArm.zRot = 0.7f;
        this.leftArm.zRot = -0.7f;

        // Shard ring counter-rotates and breathes; flares out and spins up on attack.
        this.shardRing.yRot = t * (0.03f + atk * 0.2f);
        float expand = 1.0f + Mth.sin(t * 0.15f) * 0.06f + atk * 0.4f;
        this.shardRing.xScale = expand;
        this.shardRing.zScale = expand;
        this.shardRing.y = 4.0f + bob;
    }
}
