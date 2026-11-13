package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Axii aim marker: while you hold the Sign key with Axii selected, the Axii glyph floats over the mob a release
 * would charm (WitchercraftAxiiAim, the same aim the cast uses), facing the camera, slowly turning and bobbing.
 * Only you see it. Replaces the old Glowing preview (AxiiGlow, in trash/).
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftAxiiMarkerClient {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/mob_effect/axii.png");
	private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final float SIZE = 0.6F;
	/** Gap between the top of the mob and the bottom of the glyph. */
	private static final float LIFT = 0.35F;
	private static final float BOB = 0.08F;
	private static final float BOB_SPEED = 0.15F;
	/** In-plane turn, degrees per tick (0 = upright). */
	private static final float TURN_SPEED = 2.0F;
	private static final float ALPHA = 0.95F;

	@SubscribeEvent
	public static void onSubmit(SubmitCustomGeometryEvent event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.level == null || !player.hasEffect(WitchercraftModMobEffects.AXII_SIGN)
				|| player.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime == 0)
			return;
		float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		LivingEntity target = WitchercraftAxiiAim.target(player, partial);
		if (target == null)
			return;
		float now = mc.level.getGameTime() + partial;
		Vec3 cam = event.getLevelRenderState().cameraRenderState.pos;
		Vec3 pos = target.getPosition(partial).add(0, target.getBbHeight() + LIFT + SIZE / 2 + BOB * Mth.sin(now * BOB_SPEED), 0);
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
		poseStack.mulPose(event.getLevelRenderState().cameraRenderState.orientation);
		poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(now * TURN_SPEED));
		int a = (int) (ALPHA * 255);
		float h = SIZE / 2;
		event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
			buffer.addVertex(pose, -h, -h, 0).setColor(255, 255, 255, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, 1);
			buffer.addVertex(pose, h, -h, 0).setColor(255, 255, 255, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, 1);
			buffer.addVertex(pose, h, h, 0).setColor(255, 255, 255, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, 1);
			buffer.addVertex(pose, -h, h, 0).setColor(255, 255, 255, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, 1);
		});
		poseStack.popPose();
	}
}
