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
 * Caped super-human archetype: heroic proportions (broad chest and shoulders, tapered waist) with a
 * flowing back cape and a raised chest emblem. Stands tall in a confident chest-out idle, leans into
 * a flight-ready forward pose at speed (cape billowing back), and throws a driving straight punch on
 * attack — the Compound-V / Viltrumite / costumed-super silhouette.
 */
public class HeroModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final ModelPart cape;
    private final ModelPart capeLower;
    private final float bodyXRot0;

    public HeroModel(ModelPart root) {
        super(root);
        this.cape = this.body.getChild("hero_cape");
        this.capeLower = this.cape.getChild("hero_cape_lower");
        this.bodyXRot0 = this.body.xRot;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // Broad heroic shoulder yoke and a raised chest emblem plate.
        body.addOrReplaceChild("hero_yoke",
                CubeListBuilder.create().texOffs(16, 16).addBox(-5.0f, -0.5f, -2.5f, 10.0f, 4.0f, 5.0f, new CubeDeformation(0.35f)),
                PartPose.offset(0.0f, 1.0f, 0.0f));
        body.addOrReplaceChild("hero_emblem",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -2.0f, -0.5f, 4.0f, 4.0f, 1.0f, new CubeDeformation(0.2f)),
                PartPose.offset(0.0f, 4.0f, -2.5f));

        // Two-segment flowing cape hung from the shoulders.
        PartDefinition cape = body.addOrReplaceChild("hero_cape",
                CubeListBuilder.create().texOffs(0, 32).addBox(-5.0f, 0.0f, 0.0f, 10.0f, 9.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 0.5f, 2.5f, 0.1f, 0.0f, 0.0f));
        cape.addOrReplaceChild("hero_cape_lower",
                CubeListBuilder.create().texOffs(22, 32).addBox(-5.0f, 0.0f, 0.0f, 10.0f, 9.0f, 1.0f),
                PartPose.offset(0.0f, 9.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        float atk = Anim.attack(state);

        // Confident chest-out idle; lean forward into a flight pose with speed.
        float lean = speed * 0.5f;
        this.body.xRot = bodyXRot0 - 0.04f + lean;

        // Cape: settles straight down at rest, sweeps up and back when moving or hovering.
        float breath = Mth.sin(t * 0.06f) * 0.05f;
        float lift = 0.2f + speed * 1.1f + breath;
        this.cape.xRot = lift + Mth.cos(t * 0.2f + state.walkAnimationPos * 0.3f) * (0.05f + speed * 0.15f);
        this.cape.zRot = Mth.sin(t * 0.13f) * 0.06f;
        this.capeLower.xRot = lift * 0.6f + Mth.cos(t * 0.2f + 0.8f) * (0.08f + speed * 0.2f);

        // Driving straight punch with the right arm on attack.
        if (atk > 0f) {
            this.rightArm.xRot = -1.6f * atk;
            this.rightArm.yRot = 0.2f * atk;
            this.rightArm.zRot = -0.1f * atk;
        }
    }
}
