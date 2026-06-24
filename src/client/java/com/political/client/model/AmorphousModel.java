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
 * Amorphous special-grade curse archetype: a lumpen, boneless mass studded with extra mouths and
 * eyes, sitting on a writhing blob base instead of legs, with two long boneless tendril-arms. The
 * whole body undulates and the tendrils lash when it attacks — the irregular, "wrong" silhouette of
 * a Jujutsu-Kaisen special grade (transfigured-soul / disaster vibe).
 */
public class AmorphousModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart blob;
    private final ModelPart maw;
    private final ModelPart lump;
    private final float bodyY0;

    public AmorphousModel(ModelPart root) {
        super(root);
        this.blob = this.body.getChild("amorphous_blob");
        this.maw = this.body.getChild("amorphous_maw");
        this.lump = this.head.getChild("amorphous_lump");
        this.bodyY0 = this.body.y;
    }

    public static LayerDefinition createLayer() {
        // Bloated, irregular torso; shrunken head melts into the mass.
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(1.1f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // A second, off-centre lump erupting from the skull with its own staring eyes.
        head.addOrReplaceChild("amorphous_lump",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -4.0f, -3.0f, 6.0f, 6.0f, 6.0f, new CubeDeformation(-0.5f)),
                PartPose.offsetAndRotation(3.0f, -3.0f, 1.0f, 0.3f, 0.6f, 0.4f));

        // A gaping torso maw (extra mouth) bulging off the chest.
        body.addOrReplaceChild("amorphous_maw",
                CubeListBuilder.create().texOffs(16, 16).addBox(-3.5f, -3.0f, -2.0f, 7.0f, 6.0f, 3.0f, new CubeDeformation(-0.2f)),
                PartPose.offset(-1.0f, 4.0f, -3.5f));

        // The dripping blob base that replaces the legs.
        body.addOrReplaceChild("amorphous_blob",
                CubeListBuilder.create().texOffs(0, 32).addBox(-6.0f, 0.0f, -5.0f, 12.0f, 11.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 11.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float atk = Anim.attack(state);

        // No legs — it oozes on the blob base.
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;

        // Constant nauseous undulation of the whole mass.
        float pulse = Mth.sin(t * 0.12f) * 0.12f;
        this.body.y = bodyY0 + Mth.sin(t * 0.09f) * 0.6f;
        this.body.zRot = Mth.sin(t * 0.07f) * 0.08f;
        this.blob.xRot = Mth.sin(t * 0.1f) * 0.1f;
        this.blob.zRot = Mth.cos(t * 0.08f) * 0.1f;
        this.maw.xRot = 0.2f + pulse + atk * 0.6f; // torso mouth gapes wider mid-attack
        this.lump.yRot = 0.6f + Mth.sin(t * 0.11f) * 0.2f;

        // Boneless tendril-arms: slow undulation at rest, whipping forward on attack.
        float undulate = Mth.sin(t * 0.18f) * 0.3f;
        this.rightArm.xRot = -0.3f + undulate - atk * 1.7f;
        this.leftArm.xRot = -0.3f - undulate - atk * 1.7f;
        this.rightArm.zRot = 0.5f + Mth.cos(t * 0.15f) * 0.2f;
        this.leftArm.zRot = -0.5f - Mth.cos(t * 0.15f) * 0.2f;
    }
}
