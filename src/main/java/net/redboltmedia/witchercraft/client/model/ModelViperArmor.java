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

public class ModelViperArmor extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "model_viper_armor"), "main");
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

	public ModelViperArmor(ModelPart root) {
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
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)).texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.6F)).texOffs(0, 34)
						.addBox(-3.0F, -0.5F, -2.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.2F)).texOffs(0, 26).addBox(-5.0F, 8.8F, -3.0F, 10.0F, 2.0F, 6.0F, new CubeDeformation(-0.4F)).texOffs(56, 0)
						.addBox(-1.0F, 9.3F, -3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)).texOffs(56, 2).addBox(3.0F, 10.1F, -2.95F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)).texOffs(56, 2).mirror()
						.addBox(-4.0F, 10.1F, -2.95F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("baldric_r1", CubeListBuilder.create().texOffs(0, 42).addBox(-5.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.5F, 3.1F, -2.65F, 0.0F, 0.0F, 0.83776F));
		body.addOrReplaceChild("baldric_back_r2", CubeListBuilder.create().texOffs(0, 42).addBox(-5.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.5F, 3.1F, 2.65F, 0.0F, 0.0F, 0.83776F));
		PartDefinition arm_r = partdefinition.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.36F)).mirror(false).texOffs(48, 24).mirror().addBox(-3.0F, 0.5F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.4F))
						.mirror(false).texOffs(48, 30).mirror().addBox(-3.0F, 4.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.55F)).mirror(false).texOffs(32, 26).mirror().addBox(-3.0F, 6.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.42F))
						.mirror(false),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		arm_r.addOrReplaceChild("pauldron_r3", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-5.0F, 0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.3F)).mirror(false),
				PartPose.offsetAndRotation(1.0F, -3.0F, 0.0F, 0.0F, 0.0F, -0.08727F));
		PartDefinition arm_l = partdefinition.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.36F)).texOffs(48, 24).addBox(-1.0F, 0.5F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.4F)).texOffs(48, 30)
						.addBox(-1.0F, 4.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.55F)).texOffs(32, 26).addBox(-1.0F, 6.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.42F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		arm_l.addOrReplaceChild("pauldron_r4", CubeListBuilder.create().texOffs(40, 16).addBox(0.0F, 0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-1.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.08727F));
		PartDefinition gauntlet_r = partdefinition.addOrReplaceChild("gauntlet_r", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition gauntlet_l = partdefinition.addOrReplaceChild("gauntlet_l", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition leg_r = partdefinition.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false).texOffs(22, 34).mirror()
				.addBox(-2.0F, 3.4F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition leg_l = partdefinition.addOrReplaceChild("leg_l",
				CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)).texOffs(22, 34).addBox(-2.0F, 3.4F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.5F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		PartDefinition boot_r = partdefinition.addOrReplaceChild(
				"boot_r", CubeListBuilder.create().texOffs(24, 16).mirror().addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false).texOffs(38, 36).mirror()
						.addBox(-2.5F, 6.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.15F)).mirror(false).texOffs(24, 42).mirror().addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.12F)).mirror(false),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition boot_l = partdefinition.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(24, 16).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(38, 36)
				.addBox(-2.5F, 6.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.2F)).texOffs(24, 42).addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public void setupAnim(LivingEntityRenderState state) {
	}
}