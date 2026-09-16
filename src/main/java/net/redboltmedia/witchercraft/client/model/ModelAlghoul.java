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

public class ModelAlghoul extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "model_alghoul"), "main");
	public final ModelPart root;
	public final ModelPart waist;
	public final ModelPart tail;
	public final ModelPart tail2;
	public final ModelPart back_spikes;
	public final ModelPart l_leg;
	public final ModelPart l_knee;
	public final ModelPart l_feet;
	public final ModelPart r_leg;
	public final ModelPart r_knee;
	public final ModelPart r_feet;
	public final ModelPart torso;
	public final ModelPart chest;
	public final ModelPart spikes;
	public final ModelPart head;
	public final ModelPart mouth;
	public final ModelPart head_spikes;
	public final ModelPart l_arm;
	public final ModelPart l_elbow;
	public final ModelPart l_hand;
	public final ModelPart l_hend;
	public final ModelPart r_arm;
	public final ModelPart r_elbow;
	public final ModelPart r_hand;
	public final ModelPart r_hend;

	public ModelAlghoul(ModelPart root) {
		super(root);
		this.root = root.getChild("root");
		this.waist = this.root.getChild("waist");
		this.tail = this.waist.getChild("tail");
		this.tail2 = this.tail.getChild("tail2");
		this.back_spikes = this.waist.getChild("back_spikes");
		this.l_leg = this.root.getChild("l_leg");
		this.l_knee = this.l_leg.getChild("l_knee");
		this.l_feet = this.l_knee.getChild("l_feet");
		this.r_leg = this.root.getChild("r_leg");
		this.r_knee = this.r_leg.getChild("r_knee");
		this.r_feet = this.r_knee.getChild("r_feet");
		this.torso = this.root.getChild("torso");
		this.chest = this.torso.getChild("chest");
		this.spikes = this.chest.getChild("spikes");
		this.head = this.chest.getChild("head");
		this.mouth = this.head.getChild("mouth");
		this.head_spikes = this.head.getChild("head_spikes");
		this.l_arm = this.torso.getChild("l_arm");
		this.l_elbow = this.l_arm.getChild("l_elbow");
		this.l_hand = this.l_elbow.getChild("l_hand");
		this.l_hend = this.l_hand.getChild("l_hend");
		this.r_arm = this.torso.getChild("r_arm");
		this.r_elbow = this.r_arm.getChild("r_elbow");
		this.r_hand = this.r_elbow.getChild("r_hand");
		this.r_hend = this.r_hand.getChild("r_hend");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 8.88831F, 4.75584F));
		PartDefinition waist = root.addOrReplaceChild("waist", CubeListBuilder.create(), PartPose.offset(0.0F, -0.15003F, -0.76155F));
		waist.addOrReplaceChild("cube_r0", CubeListBuilder.create().texOffs(0, 36).addBox(-3.5F, -3.0F, 1.0F, 7.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.26172F, -1.99429F, 0.04363F, 0.0F, 0.0F));
		PartDefinition tail = waist.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(12, 67).addBox(-1.0F, -1.21928F, 0.00194F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -2.03814F, 9.01568F, 0.04363F, 0.0F, 0.0F));
		PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 66).addBox(-0.5F, -0.5F, -0.2F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.21928F, 4.20194F));
		PartDefinition back_spikes = waist.addOrReplaceChild("back_spikes", CubeListBuilder.create(), PartPose.offset(0.01574F, -2.38518F, 1.61846F));
		back_spikes.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(40, 67).addBox(-1.0F, -0.92931F, -1.48416F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(2.83042F, -1.93649F, 1.1639F, -0.50558F, 0.00288F, 0.11992F));
		back_spikes.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, 52).addBox(-1.0F, -4.53535F, -1.61605F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(2.83042F, -2.13649F, 1.1639F, -0.89828F, 0.00288F, 0.11992F));
		back_spikes.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(40, 67).mirror().addBox(-1.0F, -0.92931F, -1.48416F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offsetAndRotation(-2.8619F, -1.93649F, 1.1639F, -0.50558F, -0.00288F, -0.11992F));
		back_spikes.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 52).mirror().addBox(-1.0F, -4.53535F, -1.61605F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(-2.8619F, -2.13649F, 1.1639F, -0.89828F, -0.00288F, -0.11992F));
		back_spikes.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(32, 67).addBox(-1.0F, -6.53535F, -1.61605F, 2.0F, 7.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(0.03042F, -2.63649F, 3.1639F, -0.95993F, 0.00206F, 0.0F));
		back_spikes.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(40, 67).addBox(-1.0F, -0.92931F, -1.48416F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.03042F, -2.63649F, 3.1639F, -0.74176F, 0.00206F, 0.0F));
		PartDefinition l_leg = root.addOrReplaceChild("l_leg", CubeListBuilder.create().texOffs(34, 50).addBox(-0.91111F, -1.18125F, -2.45244F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(4.41111F, 0.29294F, 3.6966F));
		PartDefinition l_knee = l_leg.addOrReplaceChild("l_knee", CubeListBuilder.create().texOffs(52, 61).addBox(-1.5F, -1.0789F, 0.8035F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.98889F, 8.89765F, 1.74406F));
		PartDefinition l_feet = l_knee.addOrReplaceChild(
				"l_feet", CubeListBuilder.create().texOffs(64, 38).addBox(-2.0F, -2.59018F, -0.43886F, 4.0F, 7.0F, 2.0F, new CubeDeformation(0.2F)).texOffs(54, 31).addBox(-2.0F, 4.43376F, -3.87053F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(54, 31).addBox(-0.5F, 4.43376F, -3.87053F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(54, 31).addBox(1.0F, 4.43376F, -3.87053F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.51128F, 6.24236F));
		l_feet.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(68, 61).addBox(1.0F, -1.25F, 1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 4.85982F, -2.43886F, 0.21817F, 0.0F, 0.0F));
		l_feet.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(68, 61).addBox(-2.0F, -1.25F, 1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 4.85982F, -2.43886F, 0.21817F, 0.0F, 0.0F));
		l_feet.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(68, 61).addBox(-0.5F, -1.25F, 1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 4.85982F, -2.43886F, 0.21817F, 0.0F, 0.0F));
		PartDefinition r_leg = root.addOrReplaceChild("r_leg", CubeListBuilder.create().texOffs(34, 50).mirror().addBox(-3.08889F, -1.18125F, -2.45244F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-4.41111F, 0.29294F, 3.6966F));
		PartDefinition r_knee = r_leg.addOrReplaceChild("r_knee", CubeListBuilder.create().texOffs(52, 61).mirror().addBox(-1.5F, -1.0789F, 0.8035F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-0.98889F, 8.89765F, 1.74406F));
		PartDefinition r_feet = r_knee.addOrReplaceChild("r_feet",
				CubeListBuilder.create().texOffs(64, 38).mirror().addBox(-2.0F, -2.59018F, -0.43886F, 4.0F, 7.0F, 2.0F, new CubeDeformation(0.2F)).mirror(false).texOffs(54, 31).mirror()
						.addBox(1.0F, 4.43376F, -3.87053F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(54, 31).mirror().addBox(-0.5F, 4.43376F, -3.87053F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(54, 31).mirror().addBox(-2.0F, 4.43376F, -3.87053F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(0.0F, 0.51128F, 6.24236F));
		r_feet.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(68, 61).mirror().addBox(-2.0F, -1.25F, 1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 4.85982F, -2.43886F, 0.21817F, 0.0F, 0.0F));
		r_feet.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(68, 61).mirror().addBox(1.0F, -1.25F, 1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 4.85982F, -2.43886F, 0.21817F, 0.0F, 0.0F));
		r_feet.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(68, 61).mirror().addBox(-0.5F, -1.25F, 1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 4.85982F, -2.43886F, 0.21817F, 0.0F, 0.0F));
		PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, -0.43583F, -6.63164F));
		PartDefinition chest = torso.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, -1.30757F, -2.06077F));
		chest.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -4.0F, -5.0F, 11.0F, 9.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.15509F, 1.23657F, 0.08727F, 0.0F, 0.0F));
		chest.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 21).addBox(-3.5F, -0.8F, -6.0F, 7.0F, 3.0F, 12.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -1.64491F, 1.93657F, -0.1309F, 0.0F, 0.0F));
		PartDefinition spikes = chest.addOrReplaceChild("spikes", CubeListBuilder.create(), PartPose.offset(0.0F, -3.13576F, 0.05212F));
		spikes.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(24, 52).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(2.99786F, -5.28294F, 0.33121F, -0.60282F, 0.05891F, 0.18314F));
		spikes.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(40, 67).addBox(-0.7F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, -1.55667F, -1.99135F, -0.47192F, 0.05891F, 0.18314F));
		spikes.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(24, 52).mirror().addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(-2.99786F, -5.28294F, 0.33121F, -0.60282F, -0.05891F, -0.18314F));
		spikes.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(40, 67).mirror().addBox(-1.3F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offsetAndRotation(-2.0F, -1.55667F, -1.99135F, -0.47192F, -0.05891F, -0.18314F));
		spikes.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(24, 67).addBox(-0.7F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, -1.55667F, 2.60865F, -0.60282F, 0.05891F, 0.18314F));
		spikes.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(68, 21).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(3.26347F, -6.14867F, 6.2686F, -0.73372F, 0.05891F, 0.18314F));
		spikes.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(68, 21).mirror().addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(-3.26347F, -6.14867F, 6.2686F, -0.73372F, -0.05891F, -0.18314F));
		spikes.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(24, 67).mirror().addBox(-1.3F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offsetAndRotation(-2.0F, -1.55667F, 2.60865F, -0.60282F, -0.05891F, -0.18314F));
		spikes.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(40, 67).addBox(-0.7F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, -0.55667F, 6.00865F, -0.60282F, 0.05891F, 0.18314F));
		spikes.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(24, 52).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(2.99786F, -3.78294F, 9.03121F, -0.82099F, 0.05891F, 0.18314F));
		spikes.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(24, 52).mirror().addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(-2.99786F, -3.78294F, 9.03121F, -0.82099F, -0.05891F, -0.18314F));
		spikes.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(40, 67).mirror().addBox(-1.3F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offsetAndRotation(-2.0F, -0.55667F, 6.00865F, -0.60282F, -0.05891F, -0.18314F));
		spikes.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(38, 31).mirror().addBox(0.0F, -2.75F, 0.6F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(2.55331F, -0.91084F, -2.34103F, -0.74892F, 0.17957F, 0.39985F));
		spikes.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(68, 47).mirror().addBox(0.0F, -0.75F, 0.6F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)).mirror(false),
				PartPose.offsetAndRotation(2.55331F, -0.91084F, -2.34103F, -0.74892F, 0.17957F, 0.39985F));
		spikes.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(68, 47).mirror().addBox(-0.3F, -3.05F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)).mirror(false),
				PartPose.offsetAndRotation(2.55331F, -0.91084F, -2.34103F, -0.74892F, 0.17957F, 0.39985F));
		spikes.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(38, 31).mirror().addBox(-0.3F, -5.05F, 3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(2.55331F, -0.91084F, -2.34103F, -0.74892F, 0.17957F, 0.39985F));
		spikes.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(68, 47).mirror().addBox(-0.3F, -3.05F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)).mirror(false),
				PartPose.offsetAndRotation(2.55331F, -0.51084F, 1.25897F, -0.74892F, 0.17957F, 0.39985F));
		spikes.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(38, 31).mirror().addBox(-0.3F, -5.05F, 3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(2.55331F, -0.51084F, 1.25897F, -0.74892F, 0.17957F, 0.39985F));
		spikes.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(38, 31).addBox(-1.7F, -5.05F, 3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(-2.55331F, -0.51084F, 1.25897F, -0.74892F, -0.17957F, -0.39985F));
		spikes.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(68, 47).addBox(-1.7F, -3.05F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)),
				PartPose.offsetAndRotation(-2.55331F, -0.51084F, 1.25897F, -0.74892F, -0.17957F, -0.39985F));
		spikes.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(38, 31).addBox(-1.7F, -5.05F, 3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(-2.55331F, -0.91084F, -2.34103F, -0.74892F, -0.17957F, -0.39985F));
		spikes.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(68, 47).addBox(-1.7F, -3.05F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)),
				PartPose.offsetAndRotation(-2.55331F, -0.91084F, -2.34103F, -0.74892F, -0.17957F, -0.39985F));
		spikes.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(38, 31).addBox(-2.0F, -2.75F, 0.6F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(-2.55331F, -0.91084F, -2.34103F, -0.74892F, -0.17957F, -0.39985F));
		spikes.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(68, 47).addBox(-2.0F, -0.75F, 0.6F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)),
				PartPose.offsetAndRotation(-2.55331F, -0.91084F, -2.34103F, -0.74892F, -0.17957F, -0.39985F));
		PartDefinition head = chest.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(34, 36).addBox(-3.5F, -3.8F, -7.5F, 7.0F, 6.0F, 8.0F, new CubeDeformation(0.01F)).texOffs(46, 12).addBox(-3.5F, 2.2F, -7.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(0.0F, 0.52061F, -4.17367F, 0.08727F, 0.0F, 0.0F));
		head.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(0, 81).mirror().addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.11F)).mirror(false),
				PartPose.offsetAndRotation(2.5F, -1.3F, -6.0F, 0.0F, 0.0F, -0.08727F));
		head.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(0, 81).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.11F)), PartPose.offsetAndRotation(-2.5F, -1.3F, -6.0F, 0.0F, 0.0F, 0.08727F));
		head.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(46, 31).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.9F, -7.1F, -0.3927F, 0.0F, 0.0F));
		PartDefinition mouth = head.addOrReplaceChild("mouth",
				CubeListBuilder.create().texOffs(38, 21).addBox(-3.5F, -1.0F, -7.6F, 7.0F, 2.0F, 8.0F, new CubeDeformation(0.1F)).texOffs(0, 52).addBox(-3.5F, -3.0F, -7.0F, 7.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 3.2F, -0.4F));
		PartDefinition head_spikes = head.addOrReplaceChild("head_spikes", CubeListBuilder.create(), PartPose.offset(0.0F, -3.79233F, -2.97546F));
		head_spikes.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(38, 31).addBox(-1.0F, -1.2F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(-2.0F, -0.20767F, -2.02454F, -0.43633F, 0.0F, 0.0F));
		head_spikes.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(38, 31).addBox(-1.0F, -3.2F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-2.0F, -0.20767F, -2.02454F, -0.43633F, 0.0F, 0.0F));
		head_spikes.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(38, 31).mirror().addBox(-1.0F, -3.2F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(2.0F, -0.20767F, -2.02454F, -0.43633F, 0.0F, 0.0F));
		head_spikes.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(38, 31).mirror().addBox(-0.4F, -1.6F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
				PartPose.offsetAndRotation(2.0F, -2.29316F, 2.25807F, -0.42721F, 0.0916F, 0.19828F));
		head_spikes.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(68, 47).mirror().addBox(-0.4F, 0.4F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)).mirror(false),
				PartPose.offsetAndRotation(2.0F, -2.29316F, 2.25807F, -0.42721F, 0.0916F, 0.19828F));
		head_spikes.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(68, 47).addBox(-1.6F, 0.4F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.4F)),
				PartPose.offsetAndRotation(-2.0F, -2.29316F, 2.25807F, -0.42721F, -0.0916F, -0.19828F));
		head_spikes.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(38, 31).addBox(-1.6F, -1.6F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(-2.0F, -2.29316F, 2.25807F, -0.42721F, -0.0916F, -0.19828F));
		head_spikes.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(38, 31).mirror().addBox(-1.0F, -1.2F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F)).mirror(false),
				PartPose.offsetAndRotation(2.0F, -0.20767F, -2.02454F, -0.43633F, 0.0F, 0.0F));
		PartDefinition l_arm = torso.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(52, 50).addBox(-2.0F, 4.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(64, 31)
				.addBox(-2.0F, 8.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.2F)).texOffs(18, 59).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)),
				PartPose.offsetAndRotation(5.5F, -1.15248F, -1.8242F, 0.2618F, 0.0F, 0.0F));
		PartDefinition l_elbow = l_arm.addOrReplaceChild("l_elbow", CubeListBuilder.create().texOffs(46, 0).addBox(-2.0F, -1.5F, -8.0F, 4.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 11.31785F, 0.74994F, 0.34994F, -0.00493F, -0.00706F));
		PartDefinition l_hand = l_elbow.addOrReplaceChild("l_hand", CubeListBuilder.create().texOffs(0, 59).addBox(-3.0F, -1.0F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.5F, -1.0F, -8.0F, -0.56723F, 0.0F, 0.0F));
		PartDefinition l_hend = l_hand.addOrReplaceChild("l_hend",
				CubeListBuilder.create().texOffs(68, 53).addBox(-2.3154F, 1.10409F, -6.24829F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(68, 53).addBox(-0.4154F, 1.10409F, -6.24829F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(68, 57).addBox(-2.3154F, 0.10409F, -3.24829F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(68, 57).addBox(-0.4154F, 0.10409F, -3.24829F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.31544F, -0.08797F, -0.75316F, 0.04363F, 0.0F, 0.0F));
		l_hend.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(68, 53).addBox(-0.3853F, -0.50781F, -2.53504F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.08456F, 1.6119F, -1.25193F, 0.0F, -0.47997F, 0.0F));
		l_hend.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(68, 57).addBox(-0.3853F, -1.50781F, 0.46496F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.08456F, 1.6119F, -1.25193F, 0.0F, -0.47997F, 0.0F));
		l_hend.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(68, 53).addBox(0.38866F, -0.24219F, -2.29149F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.39935F, 1.44779F, -0.52036F, 0.0F, 0.47997F, 0.0F));
		l_hend.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(68, 57).addBox(0.38866F, -1.24219F, 0.70851F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.39935F, 1.44779F, -0.52036F, 0.0F, 0.47997F, 0.0F));
		PartDefinition r_arm = torso.addOrReplaceChild(
				"r_arm", CubeListBuilder.create().texOffs(52, 50).mirror().addBox(-2.0F, 4.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(64, 31).mirror()
						.addBox(-2.0F, 8.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false).texOffs(18, 59).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false),
				PartPose.offsetAndRotation(-5.5F, -1.15248F, -1.8242F, 0.2618F, 0.0F, 0.0F));
		PartDefinition r_elbow = r_arm.addOrReplaceChild("r_elbow", CubeListBuilder.create().texOffs(46, 0).mirror().addBox(-2.0F, -1.5F, -8.0F, 4.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 11.31785F, 0.74994F, 0.34994F, 0.00493F, 0.00706F));
		PartDefinition r_hand = r_elbow.addOrReplaceChild("r_hand", CubeListBuilder.create().texOffs(0, 59).mirror().addBox(-2.0F, -1.0F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-0.5F, -1.0F, -8.0F, -0.56723F, 0.0F, 0.0F));
		PartDefinition r_hend = r_hand.addOrReplaceChild("r_hend",
				CubeListBuilder.create().texOffs(68, 53).mirror().addBox(1.3154F, 1.10409F, -6.24829F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(68, 53).mirror()
						.addBox(-0.5846F, 1.10409F, -6.24829F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(68, 57).mirror().addBox(1.3154F, 0.10409F, -3.24829F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(68, 57).mirror().addBox(-0.5846F, 0.10409F, -3.24829F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-0.31544F, -0.08797F, -0.75316F, 0.04363F, 0.0F, 0.0F));
		r_hend.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(68, 53).mirror().addBox(-0.6147F, -0.50781F, -2.53504F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-2.08456F, 1.6119F, -1.25193F, 0.0F, 0.47997F, 0.0F));
		r_hend.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(68, 57).mirror().addBox(-0.6147F, -1.50781F, 0.46496F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-2.08456F, 1.6119F, -1.25193F, 0.0F, 0.47997F, 0.0F));
		r_hend.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(68, 53).mirror().addBox(-1.38866F, -0.24219F, -2.29149F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(4.39935F, 1.44779F, -0.52036F, 0.0F, -0.47997F, 0.0F));
		r_hend.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(68, 57).mirror().addBox(-1.38866F, -1.24219F, 0.70851F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(4.39935F, 1.44779F, -0.52036F, 0.0F, -0.47997F, 0.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	public void setupAnim(LivingEntityRenderState state) {
	}
}