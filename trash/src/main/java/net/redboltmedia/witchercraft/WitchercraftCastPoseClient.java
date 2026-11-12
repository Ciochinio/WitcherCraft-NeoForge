package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.entity.Avatar;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.Minecraft;

/**
 * Third-person Sign casting pose: while WitchercraftCastPoseMessage says a player is casting, their
 * off-hand arm is held out forward. There is no free arm pose to borrow (custom ArmPose constants need an
 * enumextensions.json that MCreator regenerates away), so this freezes the vanilla swing of the off-hand
 * arm at CAST_SWING_PROGRESS, where the WHACK swing has the arm reaching forward and following the head's
 * pitch. A real attack swing (attackTime > 0) always wins.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class WitchercraftCastPoseClient {
	public static final float CAST_SWING_PROGRESS = 0.2F;

	@SubscribeEvent
	public static void registerModifiers(RegisterRenderStateModifiersEvent event) {
		event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
			@Override
			public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
				Long until = WitchercraftCastPoseMessage.POSED_UNTIL.get(avatar.getId());
				if (until == null || Minecraft.getInstance().level == null)
					return;
				if (Minecraft.getInstance().level.getGameTime() >= until) {
					WitchercraftCastPoseMessage.POSED_UNTIL.remove(avatar.getId());
					return;
				}
				if (state.attackTime > 0)
					return;
				state.attackArm = state.mainArm.getOpposite();
				state.attackTime = CAST_SWING_PROGRESS;
				state.swingAnimationType = SwingAnimationType.WHACK;
			}
		});
	}
}
