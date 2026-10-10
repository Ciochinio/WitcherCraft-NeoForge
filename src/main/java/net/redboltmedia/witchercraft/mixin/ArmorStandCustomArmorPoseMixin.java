package net.redboltmedia.witchercraft.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Rotations;

/**
 * Custom armor models (MCreator wraps them in a plain HumanoidModel) animate like an idle player
 * when worn by an armor stand: arms bob with the stand's age and its pose is ignored. Vanilla avoids
 * this with ArmorStandArmorModel. This applies the same pose to any other HumanoidModel rendered for
 * an armor stand, after vanilla's setupAnim, so it fixes every School set at once.
 * Mirrors ArmorStandArmorModel.setupAnim and its baked offsets (head 1 lower, legs 1 higher).
 *
 * HAND-MAINTAINED: not an MCreator element. Registered in witchercraft.mixins.json.
 */
@Mixin(HumanoidModel.class)
public abstract class ArmorStandCustomArmorPoseMixin {
	@Shadow @Final public ModelPart head;
	@Shadow @Final public ModelPart body;
	@Shadow @Final public ModelPart rightArm;
	@Shadow @Final public ModelPart leftArm;
	@Shadow @Final public ModelPart rightLeg;
	@Shadow @Final public ModelPart leftLeg;

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
	private void witchercraft$armorStandPose(HumanoidRenderState state, CallbackInfo ci) {
		if (!(state instanceof ArmorStandRenderState stand) || (Object) this instanceof ArmorStandArmorModel)
			return;
		pose(this.head, stand.headPose);
		pose(this.body, stand.bodyPose);
		pose(this.leftArm, stand.leftArmPose);
		pose(this.rightArm, stand.rightArmPose);
		pose(this.leftLeg, stand.leftLegPose);
		pose(this.rightLeg, stand.rightLegPose);
		this.head.y += 1.0F;
		this.leftLeg.y -= 1.0F;
		this.rightLeg.y -= 1.0F;
	}

	private static void pose(ModelPart part, Rotations r) {
		part.xRot = (float) (Math.PI / 180.0) * r.x();
		part.yRot = (float) (Math.PI / 180.0) * r.y();
		part.zRot = (float) (Math.PI / 180.0) * r.z();
	}
}
