public class ModelUrsineArmor extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "model_ursine_armor"), "main");
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

	public ModelUrsineArmor(ModelPart root) {
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

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
		.texOffs(40, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.5F))
		.texOffs(0, 25).addBox(-4.5F, -1.0F, -2.5F, 9.0F, 3.0F, 5.0F, new CubeDeformation(0.3F))
		.texOffs(0, 16).addBox(-1.0F, 0.5F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(22, 41).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 48).addBox(-4.5F, 10.15F, -2.5F, 9.0F, 1.0F, 5.0F, new CubeDeformation(0.15F))
		.texOffs(44, 48).addBox(0.75F, 6.0F, -3.7F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 48).addBox(-3.75F, 9.0F, -3.55F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("bandolier_r1", CubeListBuilder.create().texOffs(8, 54).addBox(-5.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-1.5F, 3.35F, -2.6F, 0.0F, 0.0F, 1.0472F));

		PartDefinition arm_r = partdefinition.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false)
		.texOffs(28, 48).mirror().addBox(-3.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false)
		.texOffs(0, 33).mirror().addBox(-3.0F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.6F)).mirror(false)
		.texOffs(58, 48).mirror().addBox(-4.25F, 7.75F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));
		arm_r.addOrReplaceChild("pauldron_r2", CubeListBuilder.create().texOffs(28, 25).mirror().addBox(-5.0F, 0.25F, -3.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(0.5F, -3.0F, 0.0F, 0.0F, 0.0F, -0.08727F));
		arm_r.addOrReplaceChild("pauldron_lame_r3", CubeListBuilder.create().texOffs(0, 41).mirror().addBox(-5.25F, 2.25F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.2F)).mirror(false), PartPose.offsetAndRotation(0.5F, -3.0F, 0.0F, 0.0F, 0.0F, -0.08727F));

		PartDefinition arm_l = partdefinition.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F))
		.texOffs(28, 48).addBox(-1.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.45F))
		.texOffs(0, 33).addBox(-1.0F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.6F))
		.texOffs(58, 48).addBox(3.25F, 7.75F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F)), PartPose.offset(5.0F, 2.0F, 0.0F));
		arm_l.addOrReplaceChild("pauldron_r4", CubeListBuilder.create().texOffs(28, 25).addBox(0.0F, 0.25F, -3.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.5F, -3.0F, 0.0F, 0.0F, 0.0F, 0.08727F));
		arm_l.addOrReplaceChild("pauldron_lame_r5", CubeListBuilder.create().texOffs(0, 41).addBox(0.25F, 2.25F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.5F, -3.0F, 0.0F, 0.0F, 0.0F, 0.08727F));

		PartDefinition gauntlet_r = partdefinition.addOrReplaceChild("gauntlet_r", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition gauntlet_l = partdefinition.addOrReplaceChild("gauntlet_l", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition leg_r = partdefinition.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(24, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.6F)).mirror(false)
		.texOffs(16, 33).mirror().addBox(-2.0F, 5.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition leg_l = partdefinition.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(24, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.65F))
		.texOffs(16, 33).addBox(-2.0F, 5.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.55F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition boot_r = partdefinition.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(32, 33).mirror().addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.4F)).mirror(false)
		.texOffs(0, 54).mirror().addBox(-1.5F, 9.5F, -2.9F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.05F)).mirror(false)
		.texOffs(30, 54).mirror().addBox(-2.0F, 11.2F, -3.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition boot_l = partdefinition.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(32, 33).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.45F))
		.texOffs(0, 54).addBox(-1.5F, 9.5F, -2.9F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.05F))
		.texOffs(30, 54).addBox(-2.0F, 11.2F, -3.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override public void setupAnim(LivingEntityRenderState state) {}
}
