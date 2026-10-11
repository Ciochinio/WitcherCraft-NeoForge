public class ModelGriffinArmor extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "model_griffin_armor"), "main");
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

	public ModelGriffinArmor(ModelPart root) {
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

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F))
		.texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.6F))
		.texOffs(24, 16).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.2F))
		.texOffs(36, 30).addBox(-3.5F, 1.8F, -2.9F, 7.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(0, 24).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
		.texOffs(56, 3).addBox(-1.0F, 8.5F, -3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.15F))
		.texOffs(56, 0).addBox(-0.5F, 9.1F, -2.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(56, 5).addBox(1.6F, 9.4F, -3.1F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.15F))
		.texOffs(56, 5).mirror().addBox(-4.6F, 9.4F, -3.1F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("baldric_r1", CubeListBuilder.create().texOffs(36, 32).addBox(-6.0F, -0.5F, -0.5F, 12.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.436F, 3.83F, -2.5F, 0.0F, 0.0F, 0.83776F));
		body.addOrReplaceChild("baldric_back_r2", CubeListBuilder.create().texOffs(36, 32).addBox(-6.0F, -0.5F, -0.5F, 12.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.436F, 3.83F, 2.5F, 0.0F, 0.0F, 0.83776F));

		PartDefinition arm_r = partdefinition.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false)
		.texOffs(36, 36).mirror().addBox(-3.0F, -1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false)
		.texOffs(20, 36).mirror().addBox(-3.0F, 5.5F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.6F)).mirror(false)
		.texOffs(48, 24).mirror().addBox(-3.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));
		arm_r.addOrReplaceChild("pauldron_r3", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-5.0F, 0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(0.5F, -3.0F, 0.0F, 0.0F, 0.0F, -0.08727F));

		PartDefinition arm_l = partdefinition.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F))
		.texOffs(36, 36).addBox(-1.0F, -1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.45F))
		.texOffs(20, 36).addBox(-1.0F, 5.5F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.6F))
		.texOffs(48, 24).addBox(-1.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(5.0F, 2.0F, 0.0F));
		arm_l.addOrReplaceChild("pauldron_r4", CubeListBuilder.create().texOffs(0, 36).addBox(0.0F, 0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-0.5F, -3.0F, 0.0F, 0.0F, 0.0F, 0.08727F));

		PartDefinition gauntlet_r = partdefinition.addOrReplaceChild("gauntlet_r", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition gauntlet_l = partdefinition.addOrReplaceChild("gauntlet_l", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition leg_r = partdefinition.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false)
		.texOffs(20, 30).mirror().addBox(-2.0F, -0.5F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
		.texOffs(28, 24).mirror().addBox(-2.5F, 4.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(56, 9).mirror().addBox(-1.5F, 3.5F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition leg_l = partdefinition.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
		.texOffs(20, 30).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.55F))
		.texOffs(28, 24).addBox(-2.5F, 4.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
		.texOffs(56, 9).addBox(-1.5F, 3.5F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.05F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition boot_r = partdefinition.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(0, 45).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false)
		.texOffs(0, 30).mirror().addBox(-2.5F, 5.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.15F)).mirror(false)
		.texOffs(52, 16).mirror().addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition boot_l = partdefinition.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F))
		.texOffs(0, 30).addBox(-2.5F, 5.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.2F))
		.texOffs(52, 16).addBox(-2.0F, 11.45F, -3.45F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override public void setupAnim(LivingEntityRenderState state) {}
}
