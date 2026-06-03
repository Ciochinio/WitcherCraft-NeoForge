// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class ModelCockatrice<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "cockatrice"), "main");
	private final ModelPart bone;
	private final ModelPart body;
	private final ModelPart waist;
	private final ModelPart tail;
	private final ModelPart tail_2;
	private final ModelPart tail_3;
	private final ModelPart tail_4;
	private final ModelPart tail_5;
	private final ModelPart upper_body;
	private final ModelPart Left_wing_el;
	private final ModelPart left_wing;
	private final ModelPart left_wing_2;
	private final ModelPart left_wing_3;
	private final ModelPart left_wing_4;
	private final ModelPart left_hand;
	private final ModelPart Right_wing_el;
	private final ModelPart right_wing;
	private final ModelPart right_wing_2;
	private final ModelPart right_wing_3;
	private final ModelPart right_wing_4;
	private final ModelPart right_hand;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart left_leg;
	private final ModelPart left_legknee;
	private final ModelPart left_leg_foot;
	private final ModelPart left_leg_finger;
	private final ModelPart left_leg_finger_a2;
	private final ModelPart left_leg_finger_a;
	private final ModelPart right_leg;
	private final ModelPart right_legknee;
	private final ModelPart right_leg_foot;
	private final ModelPart right_leg_finger;
	private final ModelPart right_leg_finger_a2;
	private final ModelPart right_leg_finger_a;

	public ModelCockatrice(ModelPart root) {
		this.bone = root.getChild("bone");
		this.body = this.bone.getChild("body");
		this.waist = this.body.getChild("waist");
		this.tail = this.waist.getChild("tail");
		this.tail_2 = this.tail.getChild("tail_2");
		this.tail_3 = this.tail_2.getChild("tail_3");
		this.tail_4 = this.tail_3.getChild("tail_4");
		this.tail_5 = this.tail_4.getChild("tail_5");
		this.upper_body = this.body.getChild("upper_body");
		this.Left_wing_el = this.upper_body.getChild("Left_wing_el");
		this.left_wing = this.Left_wing_el.getChild("left_wing");
		this.left_wing_2 = this.left_wing.getChild("left_wing_2");
		this.left_wing_3 = this.left_wing_2.getChild("left_wing_3");
		this.left_wing_4 = this.left_wing_3.getChild("left_wing_4");
		this.left_hand = this.left_wing_2.getChild("left_hand");
		this.Right_wing_el = this.upper_body.getChild("Right_wing_el");
		this.right_wing = this.Right_wing_el.getChild("right_wing");
		this.right_wing_2 = this.right_wing.getChild("right_wing_2");
		this.right_wing_3 = this.right_wing_2.getChild("right_wing_3");
		this.right_wing_4 = this.right_wing_3.getChild("right_wing_4");
		this.right_hand = this.right_wing_2.getChild("right_hand");
		this.neck = this.upper_body.getChild("neck");
		this.head = this.neck.getChild("head");
		this.left_leg = this.bone.getChild("left_leg");
		this.left_legknee = this.left_leg.getChild("left_legknee");
		this.left_leg_foot = this.left_legknee.getChild("left_leg_foot");
		this.left_leg_finger = this.left_leg_foot.getChild("left_leg_finger");
		this.left_leg_finger_a2 = this.left_leg_finger.getChild("left_leg_finger_a2");
		this.left_leg_finger_a = this.left_leg_finger.getChild("left_leg_finger_a");
		this.right_leg = this.bone.getChild("right_leg");
		this.right_legknee = this.right_leg.getChild("right_legknee");
		this.right_leg_foot = this.right_legknee.getChild("right_leg_foot");
		this.right_leg_finger = this.right_leg_foot.getChild("right_leg_finger");
		this.right_leg_finger_a2 = this.right_leg_finger.getChild("right_leg_finger_a2");
		this.right_leg_finger_a = this.right_leg_finger.getChild("right_leg_finger_a");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(),
				PartPose.offset(0.0F, -4.4661F, -1.3765F));

		PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create(),
				PartPose.offset(0.0F, -3.2961F, -20.4601F));

		PartDefinition waist = body.addOrReplaceChild("waist", CubeListBuilder.create(),
				PartPose.offset(0.0F, 3.8575F, 19.308F));

		PartDefinition cube_r1 = waist.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(168, 160)
				.addBox(0.0F, -23.9665F, 24.4958F, 0.0F, 5.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(130, 160)
				.addBox(-2.0F, -18.9665F, 24.4958F, 4.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(94, 110)
				.addBox(-7.0F, -16.9665F, 24.4958F, 14.0F, 14.0F, 16.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.8453F, -26.9714F, -0.3927F, 0.0F, 0.0F));

		PartDefinition tail = waist.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 166).addBox(-3.5F,
				-3.5F, -1.7143F, 7.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.6882F, 15.2387F));

		PartDefinition tail_2 = tail.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(130, 140)
				.addBox(-3.0F, -3.0F, 0.4167F, 6.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 8.869F));

		PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(88, 140)
				.addBox(-2.0F, -2.0F, 0.7F, 4.0F, 4.0F, 17.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 13.7167F));

		PartDefinition tail_4 = tail_3.addOrReplaceChild("tail_4", CubeListBuilder.create().texOffs(142, 177).addBox(
				-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 17.7F));

		PartDefinition tail_5 = tail_4.addOrReplaceChild("tail_5",
				CubeListBuilder.create().texOffs(176, 14)
						.addBox(-2.5F, -2.5F, -0.5F, 5.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(54, 185)
						.addBox(-2.0F, -2.0F, 8.5F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(186, 119)
						.addBox(-1.5F, -1.5F, 11.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 9.5F));

		PartDefinition upper_body = body.addOrReplaceChild("upper_body",
				CubeListBuilder.create().texOffs(104, 70)
						.addBox(-9.0F, -9.25F, -21.0F, 18.0F, 18.0F, 22.0F, new CubeDeformation(0.0F)).texOffs(124, 0)
						.addBox(-2.0F, -10.25F, -21.0F, 4.0F, 1.0F, 22.0F, new CubeDeformation(0.0F)).texOffs(55, -22)
						.addBox(0.0F, -15.25F, -21.0F, 0.0F, 5.0F, 22.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 3.2622F, 20.8366F));

		PartDefinition Left_wing_el = upper_body.addOrReplaceChild("Left_wing_el", CubeListBuilder.create()
				.texOffs(-9, 72).addBox(-0.1667F, -0.3333F, -14.0F, 9.0F, 0.0F, 34.0F, new CubeDeformation(0.0F))
				.texOffs(54, 179).addBox(-0.1667F, -1.3333F, -3.0F, 9.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)),
				PartPose.offset(7.1667F, -3.9167F, -17.0F));

		PartDefinition left_wing = Left_wing_el.addOrReplaceChild("left_wing",
				CubeListBuilder.create().texOffs(3, 36)
						.addBox(0.0F, -0.5F, -12.5F, 18.0F, 0.0F, 34.0F, new CubeDeformation(0.0F)).texOffs(186, 107)
						.addBox(0.02F, -1.5F, -1.5F, 3.98F, 3.0F, 3.0F, new CubeDeformation(0.01F)),
				PartPose.offset(8.8333F, 0.1667F, -1.5F));

		PartDefinition cube_r2 = left_wing
				.addOrReplaceChild("cube_r2",
						CubeListBuilder.create().texOffs(124, 23).addBox(-5.5F, -1.5F, -6.5F, 21.0F, 3.0F, 3.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(10.5F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition left_wing_2 = left_wing.addOrReplaceChild("left_wing_2",
				CubeListBuilder.create().texOffs(124, 29)
						.addBox(0.5F, -1.5F, -1.5F, 19.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)).texOffs(-35, 36)
						.addBox(1.5F, -0.51F, 1.5F, 18.0F, 0.0F, 35.0F, new CubeDeformation(0.0F)),
				PartPose.offset(16.5F, 0.0F, -14.0F));

		PartDefinition left_wing_3 = left_wing_2.addOrReplaceChild("left_wing_3", CubeListBuilder.create()
				.texOffs(170, 152).addBox(0.566F, -1.33F, -1.7572F, 11.98F, 3.0F, 3.0F, new CubeDeformation(0.01F))
				.texOffs(-35, 72).addBox(0.546F, -0.34F, 1.2428F, 12.0F, 0.0F, 35.0F, new CubeDeformation(0.0F)),
				PartPose.offset(18.954F, -0.17F, 0.2572F));

		PartDefinition left_wing_4 = left_wing_3.addOrReplaceChild("left_wing_4", CubeListBuilder.create()
				.texOffs(-35, 0).addBox(0.324F, -0.255F, 1.6142F, 27.0F, 0.0F, 35.0F, new CubeDeformation(0.0F)),
				PartPose.offset(12.222F, -0.085F, -0.3714F));

		PartDefinition cube_r3 = left_wing_4.addOrReplaceChild("cube_r3",
				CubeListBuilder.create().texOffs(168, 29).addBox(-7.0F, -1.5F, 1.5F, 14.0F, 3.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.324F, 0.255F, 0.1142F, 0.0F, -0.3927F, 0.0F));

		PartDefinition left_hand = left_wing_2.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(184, 76)
				.addBox(-2.0F, -1.5336F, -4.8603F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.01F)),
				PartPose.offset(19.5F, 0.0336F, -1.6397F));

		PartDefinition cube_r4 = left_hand.addOrReplaceChild("cube_r4",
				CubeListBuilder.create().texOffs(146, 189)
						.addBox(0.0F, -1.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(172, 23)
						.addBox(-0.5F, -1.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(186, 180)
						.addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(146, 189)
						.addBox(3.0F, -1.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(172, 23)
						.addBox(2.5F, -1.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(186, 180)
						.addBox(2.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5F, -2.5336F, -6.5603F, 0.7418F, 0.0F, 0.0F));

		PartDefinition cube_r5 = left_hand.addOrReplaceChild("cube_r5",
				CubeListBuilder.create().texOffs(186, 125)
						.addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)).texOffs(186, 125)
						.addBox(2.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)),
				PartPose.offsetAndRotation(-1.5F, -0.5336F, -4.1602F, -0.5236F, 0.0F, 0.0F));

		PartDefinition Right_wing_el = upper_body.addOrReplaceChild("Right_wing_el",
				CubeListBuilder.create().texOffs(-9, 72).mirror()
						.addBox(-8.8333F, -0.3333F, -14.0F, 9.0F, 0.0F, 34.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(54, 179).mirror()
						.addBox(-8.8333F, -1.3333F, -3.0F, 9.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false),
				PartPose.offset(-7.1667F, -3.9167F, -17.0F));

		PartDefinition right_wing = Right_wing_el.addOrReplaceChild("right_wing",
				CubeListBuilder.create().texOffs(3, 36).mirror()
						.addBox(-18.0F, -0.5F, -12.5F, 18.0F, 0.0F, 34.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(186, 107).mirror()
						.addBox(-4.0F, -1.5F, -1.5F, 3.98F, 3.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false),
				PartPose.offset(-8.8333F, 0.1667F, -1.5F));

		PartDefinition cube_r6 = right_wing.addOrReplaceChild("cube_r6",
				CubeListBuilder.create().texOffs(124, 23).mirror()
						.addBox(-15.5F, -1.5F, -6.5F, 21.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-10.5F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition right_wing_2 = right_wing.addOrReplaceChild("right_wing_2",
				CubeListBuilder.create().texOffs(124, 29).mirror()
						.addBox(-19.5F, -1.5F, -1.5F, 19.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false)
						.texOffs(-35, 36).mirror()
						.addBox(-19.5F, -0.51F, 1.5F, 18.0F, 0.0F, 35.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-16.5F, 0.0F, -14.0F));

		PartDefinition right_wing_3 = right_wing_2.addOrReplaceChild("right_wing_3",
				CubeListBuilder.create().texOffs(170, 152).mirror()
						.addBox(-12.546F, -1.33F, -1.7572F, 11.98F, 3.0F, 3.0F, new CubeDeformation(0.01F))
						.mirror(false).texOffs(-35, 72).mirror()
						.addBox(-12.546F, -0.34F, 1.2428F, 12.0F, 0.0F, 35.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-18.954F, -0.17F, 0.2572F));

		PartDefinition right_wing_4 = right_wing_3.addOrReplaceChild("right_wing_4",
				CubeListBuilder.create().texOffs(-35, 0).mirror()
						.addBox(-27.324F, -0.255F, 1.6142F, 27.0F, 0.0F, 35.0F, new CubeDeformation(0.0F))
						.mirror(false),
				PartPose.offset(-12.222F, -0.085F, -0.3714F));

		PartDefinition cube_r7 = right_wing_4.addOrReplaceChild("cube_r7",
				CubeListBuilder.create().texOffs(168, 29).mirror()
						.addBox(-7.0F, -1.5F, 1.5F, 14.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.324F, 0.255F, 0.1142F, 0.0F, 0.3927F, 0.0F));

		PartDefinition right_hand = right_wing_2.addOrReplaceChild("right_hand",
				CubeListBuilder.create().texOffs(184, 76).mirror()
						.addBox(-2.0F, -1.5336F, -4.8603F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.01F)).mirror(false),
				PartPose.offset(-19.5F, 0.0336F, -1.6397F));

		PartDefinition cube_r8 = right_hand.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(146, 189)
				.mirror().addBox(0.0F, -1.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(172, 23).mirror().addBox(-0.5F, -1.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.mirror(false).texOffs(186, 180).mirror()
				.addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(146, 189).mirror().addBox(-3.0F, -1.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.mirror(false).texOffs(172, 23).mirror()
				.addBox(-3.5F, -1.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(186, 180).mirror().addBox(-4.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.mirror(false), PartPose.offsetAndRotation(1.5F, -2.5336F, -6.5603F, 0.7418F, 0.0F, 0.0F));

		PartDefinition cube_r9 = right_hand.addOrReplaceChild("cube_r9",
				CubeListBuilder.create().texOffs(186, 125).mirror()
						.addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)).mirror(false)
						.texOffs(186, 125).mirror()
						.addBox(-4.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)).mirror(false),
				PartPose.offsetAndRotation(1.5F, -0.5336F, -4.1602F, -0.5236F, 0.0F, 0.0F));

		PartDefinition neck = upper_body.addOrReplaceChild("neck",
				CubeListBuilder.create().texOffs(82, 161)
						.addBox(0.0F, 4.7F, -16.4F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(176, 0)
						.addBox(-4.0F, -3.3F, -11.4F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 227)
						.addBox(-2.0F, -4.3F, -11.4F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(170, 136)
						.addBox(-5.0F, -4.3F, -5.4F, 10.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(184, 69)
						.addBox(-2.0F, -5.3F, -5.4F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.7F, -20.5F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(126, 187)
				.addBox(-1.0F, -2.444F, -13.6F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(154, 136)
				.addBox(-1.5F, -0.444F, -15.6F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.01F)).texOffs(184, 99)
				.addBox(-1.5F, -1.844F, -14.2F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.02F)).texOffs(184, 92)
				.addBox(-1.5F, 1.556F, -15.6F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.03F)).texOffs(44, 139)
				.addBox(-5.5F, -4.844F, -10.6F, 11.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(3, 215)
				.addBox(0.5F, 3.156F, -11.6F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(186, 131)
				.addBox(0.5F, 8.156F, -11.1F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(3, 215).mirror()
				.addBox(-2.5F, 3.156F, -11.6F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(186, 131).mirror().addBox(-2.5F, 8.156F, -11.1F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.mirror(false).texOffs(0, 196)
				.addBox(-1.0F, -12.444F, -13.6F, 2.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(108, 187)
				.addBox(-1.0F, -7.444F, -15.6F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(184, 84)
				.addBox(-1.0F, -14.444F, -10.6F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(36, 166)
				.addBox(-1.0F, -9.444F, -6.6F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(68, 185)
				.addBox(5.5F, -2.844F, -9.6F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(102, 0)
				.addBox(5.5F, -2.844F, -9.6F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.1F)).texOffs(116, 187)
				.addBox(5.5F, 0.156F, -9.6F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.1F)).texOffs(102, 0).mirror()
				.addBox(-6.5F, -2.844F, -9.6F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
				.texOffs(68, 185).mirror().addBox(-6.5F, -2.844F, -9.6F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.mirror(false).texOffs(116, 187).mirror()
				.addBox(-6.5F, 0.156F, -9.6F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
				.texOffs(94, 183).addBox(-1.0F, -11.444F, -4.6F, 2.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(36, 175).addBox(-1.0F, -11.444F, 0.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(152, 189).addBox(-1.0F, -6.444F, 0.4F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(186, 186).addBox(-1.0F, -6.444F, 1.4F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(164, 136).addBox(-1.0F, -8.444F, 5.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(186, 113).addBox(-1.0F, -8.444F, 1.4F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(164, 138).addBox(-1.0F, -4.444F, 4.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -0.456F, -11.8F));

		PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10",
				CubeListBuilder.create().texOffs(136, 189).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 1.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.144F, -14.2F, 0.7854F, 0.0F, 0.0F));

		PartDefinition left_leg = bone.addOrReplaceChild("left_leg", CubeListBuilder.create(),
				PartPose.offset(8.0F, 1.6481F, 10.2301F));

		PartDefinition cube_r11 = left_leg.addOrReplaceChild("cube_r11",
				CubeListBuilder.create().texOffs(154, 110).addBox(-3.0F, -11.0F, -10.0F, 8.0F, 18.0F, 8.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

		PartDefinition left_legknee = left_leg.addOrReplaceChild("left_legknee", CubeListBuilder.create()
				.texOffs(44, 160).addBox(-3.0F, -3.0F, -0.6667F, 6.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.0F, 14.5F, -4.8333F));

		PartDefinition left_leg_foot = left_legknee.addOrReplaceChild("left_leg_foot", CubeListBuilder.create(),
				PartPose.offset(0.0F, -0.356F, 10.4713F));

		PartDefinition cube_r12 = left_leg_foot.addOrReplaceChild("cube_r12",
				CubeListBuilder.create().texOffs(114, 161).addBox(-2.0F, 0.5F, 4.5F, 4.0F, 12.0F, 4.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 2.856F, -5.638F, 0.3927F, 0.0F, 0.0F));

		PartDefinition left_leg_finger = left_leg_foot.addOrReplaceChild("left_leg_finger", CubeListBuilder.create(),
				PartPose.offset(2.5F, 10.7921F, 3.8549F));

		PartDefinition cube_r13 = left_leg_finger.addOrReplaceChild("cube_r13",
				CubeListBuilder.create().texOffs(114, 177).addBox(-6.0F, -1.0F, -1.0F, 7.0F, 3.0F, 7.0F,
						new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(0.0F, -2.3F, -4.2F, -0.3927F, 0.0F, 0.0F));

		PartDefinition left_leg_finger_a2 = left_leg_finger.addOrReplaceChild("left_leg_finger_a2",
				CubeListBuilder.create(), PartPose.offset(-5.0F, -3.0F, -4.5F));

		PartDefinition left_leg_finger_a = left_leg_finger.addOrReplaceChild("left_leg_finger_a",
				CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -4.5F));

		PartDefinition cube_r14 = left_leg_finger_a.addOrReplaceChild("cube_r14",
				CubeListBuilder.create().texOffs(78, 183)
						.addBox(0.0F, -4.0F, -9.8F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(78, 183)
						.addBox(5.0F, -4.0F, -9.8F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.0F, 3.5F, -0.3F, 0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r15 = left_leg_finger_a.addOrReplaceChild("cube_r15",
				CubeListBuilder.create().texOffs(166, 180)
						.addBox(-1.25F, -2.75F, -5.55F, 3.0F, 3.0F, 7.0F, new CubeDeformation(-0.25F)).texOffs(166, 180)
						.addBox(3.75F, -2.75F, -5.55F, 3.0F, 3.0F, 7.0F, new CubeDeformation(-0.25F)),
				PartPose.offsetAndRotation(-5.0F, 2.4F, -0.3F, 0.3927F, 0.0F, 0.0F));

		PartDefinition right_leg = bone.addOrReplaceChild("right_leg", CubeListBuilder.create(),
				PartPose.offset(-8.0F, 1.6481F, 10.2301F));

		PartDefinition cube_r16 = right_leg.addOrReplaceChild("cube_r16",
				CubeListBuilder.create().texOffs(154, 110).mirror()
						.addBox(-5.0F, -11.0F, -10.0F, 8.0F, 18.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

		PartDefinition right_legknee = right_leg.addOrReplaceChild("right_legknee",
				CubeListBuilder.create().texOffs(44, 160).mirror()
						.addBox(-3.0F, -3.0F, -0.6667F, 6.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-1.0F, 14.5F, -4.8333F));

		PartDefinition right_leg_foot = right_legknee.addOrReplaceChild("right_leg_foot", CubeListBuilder.create(),
				PartPose.offset(0.0F, -0.356F, 10.4713F));

		PartDefinition cube_r17 = right_leg_foot.addOrReplaceChild("cube_r17",
				CubeListBuilder.create().texOffs(114, 161).mirror()
						.addBox(-2.0F, 0.5F, 4.5F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 2.856F, -5.638F, 0.3927F, 0.0F, 0.0F));

		PartDefinition right_leg_finger = right_leg_foot.addOrReplaceChild("right_leg_finger", CubeListBuilder.create(),
				PartPose.offset(-2.5F, 10.7921F, 3.8549F));

		PartDefinition cube_r18 = right_leg_finger.addOrReplaceChild("cube_r18",
				CubeListBuilder.create().texOffs(114, 177).mirror()
						.addBox(-1.0F, -1.0F, -1.0F, 7.0F, 3.0F, 7.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, -2.3F, -4.2F, -0.3927F, 0.0F, 0.0F));

		PartDefinition right_leg_finger_a2 = right_leg_finger.addOrReplaceChild("right_leg_finger_a2",
				CubeListBuilder.create(), PartPose.offset(5.0F, -3.0F, -4.5F));

		PartDefinition right_leg_finger_a = right_leg_finger.addOrReplaceChild("right_leg_finger_a",
				CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -4.5F));

		PartDefinition cube_r19 = right_leg_finger_a.addOrReplaceChild("cube_r19",
				CubeListBuilder.create().texOffs(78, 183).mirror()
						.addBox(0.0F, -4.0F, -9.8F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(78, 183).mirror()
						.addBox(-5.0F, -4.0F, -9.8F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(5.0F, 3.5F, -0.3F, 0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r20 = right_leg_finger_a.addOrReplaceChild("cube_r20",
				CubeListBuilder.create().texOffs(166, 180).mirror()
						.addBox(-1.75F, -2.75F, -5.55F, 3.0F, 3.0F, 7.0F, new CubeDeformation(-0.25F)).mirror(false)
						.texOffs(166, 180).mirror()
						.addBox(-6.75F, -2.75F, -5.55F, 3.0F, 3.0F, 7.0F, new CubeDeformation(-0.25F)).mirror(false),
				PartPose.offsetAndRotation(5.0F, 2.4F, -0.3F, 0.3927F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}