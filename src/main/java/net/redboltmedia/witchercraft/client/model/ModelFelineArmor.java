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

public class ModelFelineArmor extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "model_feline_armor"), "main");
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

	public ModelFelineArmor(ModelPart root) {
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
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)).texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.6F)).texOffs(40, 38)
						.addBox(-2.0F, 2.5F, -2.9F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)).texOffs(40, 38).addBox(-2.0F, 4.5F, -2.9F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)).texOffs(24, 16)
						.addBox(-5.0F, 6.1F, -3.0F, 10.0F, 2.0F, 6.0F, new CubeDeformation(-0.4F)).texOffs(32, 40).addBox(-3.9F, 8.1F, -3.4F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(48, 40)
						.addBox(-4.6F, 8.65F, -3.3F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)).texOffs(12, 40).addBox(0.6F, 8.65F, -3.3F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("baldric_r1", CubeListBuilder.create().texOffs(16, 38).addBox(-5.5F, -0.5F, -0.5F, 11.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.725F, 3.91F, -2.6F, 0.0F, 0.0F, 0.83776F));
		body.addOrReplaceChild("baldric_back_r2", CubeListBuilder.create().texOffs(16, 38).addBox(-5.5F, -0.5F, -0.5F, 11.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.725F, 3.91F, 2.6F, 0.0F, 0.0F, 0.83776F));
		body.addOrReplaceChild("belt_low_r3", CubeListBuilder.create().texOffs(0, 46).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 2.0F, 6.0F, new CubeDeformation(-0.35F)), PartPose.offsetAndRotation(0.0F, 8.6F, 0.0F, 0.0F, 0.0F, 0.06981F));
		PartDefinition arm_r = partdefinition.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(32, 24).addBox(-3.5F, -2.2F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.25F)).texOffs(32, 32)
						.addBox(-3.5F, 3.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.3F)).texOffs(16, 24).addBox(-3.0F, 5.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.55F)).texOffs(16, 32)
						.addBox(-3.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition arm_l = partdefinition.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(32, 24).addBox(-1.5F, -2.2F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.25F)).texOffs(32, 32)
						.addBox(-1.5F, 3.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.3F)).texOffs(16, 24).addBox(-1.0F, 5.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.55F)).texOffs(16, 32)
						.addBox(-1.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition gauntlet_r = partdefinition.addOrReplaceChild("gauntlet_r", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition gauntlet_l = partdefinition.addOrReplaceChild("gauntlet_l", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition leg_r = partdefinition.addOrReplaceChild("leg_r",
				CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(0, 38).addBox(-2.0F, 3.5F, -3.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition leg_l = partdefinition.addOrReplaceChild("leg_l",
				CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)).texOffs(0, 38).addBox(-2.0F, 3.5F, -3.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.45F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		PartDefinition boot_r = partdefinition.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(0, 24).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.45F)).texOffs(0, 56)
				.addBox(-2.5F, 6.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.15F)).texOffs(38, 40).addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition boot_l = partdefinition.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(0, 24).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(0, 56)
				.addBox(-2.5F, 6.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.2F)).texOffs(38, 40).addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public void setupAnim(LivingEntityRenderState state) {
	}
}