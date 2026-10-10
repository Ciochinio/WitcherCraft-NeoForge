package net.redboltmedia.witchercraft.client.model;

import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

public class ModelManticoreArmor extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "model_manticore_armor"), "main");
	public final ModelPart helmet;
	public final ModelPart body;
	public final ModelPart arm_r;
	public final ModelPart arm_l;
	public final ModelPart gauntlet_r;
	public final ModelPart gauntlet_l;
	public final ModelPart leg_r;
	public final ModelPart leg_l;
	public final ModelPart boot_r;
	public final ModelPart boot_l;

	public ModelManticoreArmor(ModelPart root) {
		super(root);
		this.helmet = root.getChild("helmet");
		this.body = root.getChild("body");
		this.arm_r = root.getChild("arm_r");
		this.arm_l = root.getChild("arm_l");
		this.gauntlet_r = root.getChild("gauntlet_r");
		this.gauntlet_l = root.getChild("gauntlet_l");
		this.leg_r = root.getChild("leg_r");
		this.leg_l = root.getChild("leg_l");
		this.boot_r = root.getChild("boot_r");
		this.boot_l = root.getChild("boot_l");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition helmet = partdefinition.addOrReplaceChild("helmet", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition body = partdefinition.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)).texOffs(32, 16).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.1F)).texOffs(32, 23)
						.addBox(-4.5F, 8.5F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.1F)).texOffs(56, 9).addBox(-1.0F, 8.5F, -3.25F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(56, 2)
						.addBox(-1.0F, 7.3F, -3.15F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(56, 2).addBox(-1.0F, 6.1F, -3.15F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(56, 2)
						.addBox(-1.0F, 4.9F, -3.15F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(26, 26).addBox(-4.3F, 5.5F, -3.45F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("strap_knot_r1", CubeListBuilder.create().texOffs(40, 39).addBox(-4.5F, -0.5F, -1.0F, 9.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-0.912F, 3.035F, -1.9F, 0.0F, 0.0F, 0.90757F));
		body.addOrReplaceChild("strap_knot_side_r2", CubeListBuilder.create().texOffs(40, 42).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(2.939F, 6.773F, -1.9F, 0.0F, 0.0F, 0.23562F));
		body.addOrReplaceChild("strap_plate_r3", CubeListBuilder.create().texOffs(56, 12).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.35F)), PartPose.offsetAndRotation(1.8F, 6.5F, -2.85F, 0.0F, 0.0F, 0.90757F));
		body.addOrReplaceChild("strap_tip_r4", CubeListBuilder.create().texOffs(60, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(2.509F, 7.406F, -2.6F, 0.0F, 0.0F, 0.90757F));
		body.addOrReplaceChild("strap_vials_r5", CubeListBuilder.create().texOffs(0, 46).addBox(-5.0F, -0.5F, -1.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.196F, 2.621F, -2.25F, 0.0F, 0.0F, -0.72431F));
		body.addOrReplaceChild("vials_r6", CubeListBuilder.create().texOffs(52, 42).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(1.6F, 1.38F, -2.6F, 0.0F, 0.0F, -0.72431F));
		body.addOrReplaceChild("strap_knot_back_r7", CubeListBuilder.create().texOffs(24, 46).addBox(-5.5F, -0.5F, -1.0F, 11.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.27F, 3.345F, 1.9F, 0.0F, 0.0F, 0.76794F));
		body.addOrReplaceChild("strap_vials_back_r8", CubeListBuilder.create().texOffs(0, 49).addBox(-5.5F, -0.5F, -1.0F, 11.0F, 1.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.189F, 2.886F, 2.25F, 0.0F, 0.0F, -0.71558F));
		PartDefinition arm_r = partdefinition.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false).texOffs(0, 32).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
						.mirror(false).texOffs(0, 39).mirror().addBox(-3.5F, 3.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(-0.05F)).mirror(false).texOffs(56, 0).mirror().addBox(-4.2F, 3.35F, -1.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
						.mirror(false).texOffs(56, 0).mirror().addBox(-4.2F, 3.35F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).mirror(false).texOffs(36, 32).mirror().addBox(-3.5F, 4.8F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.15F))
						.mirror(false).texOffs(20, 32).mirror().addBox(-3.0F, 7.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition arm_l = partdefinition.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(0, 32).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 39)
						.addBox(-1.5F, 3.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(-0.05F)).texOffs(56, 0).addBox(3.2F, 3.35F, -1.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(56, 0)
						.addBox(3.2F, 3.35F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(36, 32).addBox(-1.5F, 4.8F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.15F)).texOffs(20, 32)
						.addBox(-1.0F, 7.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.45F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition gauntlet_r = partdefinition.addOrReplaceChild("gauntlet_r", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition gauntlet_l = partdefinition.addOrReplaceChild("gauntlet_l", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition leg_r = partdefinition.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition leg_l = partdefinition.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		PartDefinition boot_r = partdefinition.addOrReplaceChild(
				"boot_r", CubeListBuilder.create().texOffs(16, 16).mirror().addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false).texOffs(20, 39).mirror()
						.addBox(-2.5F, 7.3F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false).texOffs(16, 26).mirror().addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).mirror(false),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition boot_l = partdefinition.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(20, 39)
				.addBox(-2.5F, 7.3F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.3F)).texOffs(16, 26).addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public void setupAnim(LivingEntityRenderState state) {
	}
}