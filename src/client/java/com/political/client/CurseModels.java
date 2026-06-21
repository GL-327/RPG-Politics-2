package com.political.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/**
 * Custom model layer for the Curse spirit: the humanoid rig (so it inherits walk/attack/idle
 * animations for free) given a distinct cursed silhouette with a pair of swept-back horns. The
 * geometry stays on the standard 64x64 humanoid UV layout so {@code curse_spirit.png} maps
 * cleanly, with the horns painted into a free corner of the sheet.
 */
public final class CurseModels {

    public static final ModelLayerLocation CURSE_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("politicalserver", "curse_spirit"), "main");

    private CurseModels() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.25f), 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");

        // Two curved horns sprouting from the temples (texOffs in a free 64x64 region).
        head.addOrReplaceChild("left_horn",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5f, -4.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offsetAndRotation(-3.0f, -7.5f, 0.0f, -0.35f, 0.0f, -0.5f));
        head.addOrReplaceChild("right_horn",
                CubeListBuilder.create().texOffs(60, 0).addBox(-0.5f, -4.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offsetAndRotation(3.0f, -7.5f, 0.0f, -0.35f, 0.0f, 0.5f));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
