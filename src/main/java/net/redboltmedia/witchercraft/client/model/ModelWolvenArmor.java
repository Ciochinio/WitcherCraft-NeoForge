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

public class ModelWolvenArmor extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "model_wolven_armor"), "main");
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

	public ModelWolvenArmor(ModelPart root) {
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
				CubeListBuilder.create().texOffs(24, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.65F)).texOffs(0, 0).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(24, 9)
						.addBox(-4.0F, 9.5F, -2.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.6F)).texOffs(0, 9).addBox(-4.0F, 10.5F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(54, 2)
						.addBox(0.5F, 9.0F, -3.15F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 23).addBox(-1.5F, 4.0F, -2.75F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(54, 0)
						.addBox(-1.0F, 7.4F, -3.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(54, 0).addBox(-1.0F, 6.1F, -3.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(54, 0)
						.addBox(-1.0F, 3.7F, -3.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)).texOffs(48, 47).addBox(0.0F, 1.5F, -3.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("chest_strap_r1", CubeListBuilder.create().texOffs(0, 47).addBox(-4.5F, -0.5F, -3.0F, 9.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 0.0F, 0.0F, 0.0F, 0.20944F));
		PartDefinition arm_r = partdefinition.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false).texOffs(36, 15).mirror()
						.addBox(-3.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false).texOffs(36, 28).mirror().addBox(-3.0F, 7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.7F)).mirror(false).texOffs(36, 21).mirror()
						.addBox(-3.0F, 5.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.6F)).mirror(false).texOffs(52, 15).mirror().addBox(-4.15F, 5.5F, -1.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)).mirror(false).texOffs(52, 15).mirror()
						.addBox(-4.15F, 5.5F, 0.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		arm_r.addOrReplaceChild("pauldron_r2", CubeListBuilder.create().texOffs(16, 15).mirror().addBox(-4.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false),
				PartPose.offsetAndRotation(0.25F, -2.5F, 0.0F, 0.0F, 0.0F, -0.08727F));
		arm_r.addOrReplaceChild("pauldron_lame_r3", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-4.25F, 2.0F, -2.5F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.15F)).mirror(false),
				PartPose.offsetAndRotation(0.25F, -2.5F, 0.0F, 0.0F, 0.0F, -0.08727F));
		arm_r.addOrReplaceChild("shoulder_strap_r4", CubeListBuilder.create().texOffs(32, 47).mirror().addBox(-2.25F, -0.5F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.25F, -2.5F, 0.0F, 0.0F, 0.0F, -0.08727F));
		PartDefinition arm_l = partdefinition.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(36, 15).addBox(-1.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)).texOffs(36, 28)
						.addBox(-1.0F, 7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.7F)).texOffs(36, 21).addBox(-1.0F, 5.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.6F)).texOffs(52, 15)
						.addBox(3.15F, 5.5F, -1.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)).texOffs(52, 15).addBox(3.15F, 5.5F, 0.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		arm_l.addOrReplaceChild("pauldron_r5", CubeListBuilder.create().texOffs(16, 15).addBox(0.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.25F, -2.5F, 0.0F, 0.0F, 0.0F, 0.08727F));
		arm_l.addOrReplaceChild("pauldron_lame_r6", CubeListBuilder.create().texOffs(0, 54).addBox(0.25F, 2.0F, -2.5F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(-0.25F, -2.5F, 0.0F, 0.0F, 0.0F, 0.08727F));
		arm_l.addOrReplaceChild("shoulder_strap_r7", CubeListBuilder.create().texOffs(32, 47).addBox(0.25F, -0.5F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, -2.5F, 0.0F, 0.0F, 0.0F, 0.08727F));
		PartDefinition gauntlet_r = partdefinition.addOrReplaceChild("gauntlet_r", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition gauntlet_l = partdefinition.addOrReplaceChild("gauntlet_l", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition leg_r = partdefinition.addOrReplaceChild(
				"leg_r", CubeListBuilder.create().texOffs(0, 33).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false).texOffs(16, 33).mirror()
						.addBox(-2.0F, 2.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false).texOffs(16, 38).mirror().addBox(-1.5F, 4.8F, -3.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition leg_l = partdefinition.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(16, 33)
				.addBox(-2.0F, 2.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.45F)).texOffs(16, 38).addBox(-1.5F, 4.8F, -3.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		PartDefinition boot_r = partdefinition.addOrReplaceChild(
				"boot_r", CubeListBuilder.create().texOffs(36, 33).mirror().addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false).texOffs(36, 42).mirror()
						.addBox(-2.0F, 7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.75F)).mirror(false).texOffs(52, 33).mirror().addBox(-2.0F, 10.5F, -3.1F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition boot_l = partdefinition.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(36, 33).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(36, 42)
				.addBox(-2.0F, 7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.75F)).texOffs(52, 33).addBox(-2.0F, 10.5F, -3.1F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public void setupAnim(LivingEntityRenderState state) {
	}
}