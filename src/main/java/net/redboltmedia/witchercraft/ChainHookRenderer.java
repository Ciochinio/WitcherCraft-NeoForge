package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModItems;

import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.Minecraft;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import org.jspecify.annotations.Nullable;

/**
 * Client side of the Chain Hook.
 *
 * Entity: draws a {@link ChainHookEntity} as a camera-facing hook head plus a chain
 * from the owner's hand to the hook. The chain is the 2D chain from vanilla's iron chain
 * item sprite (round and side-on links alternating), built like a vanilla item: every
 * opaque pixel is a 1/16-block cube, so it has 1-pixel-thick edges. It repeats every
 * LINK_TILE_LENGTH, and each tile turns around the chain's own axis to face the
 * camera, like a beacon beam, so the art is never seen edge-on. The head is hidden
 * while the hook is attached to a target, so the chain ends in the body. Hand placement
 * follows vanilla FishingHookRenderer, including the first-person near-plane offset.
 *
 * Item: while the Chain Hook is charging, the first-person item whirls in a vertical
 * circle around the hand (pitch), faster at each stage (see ChainHookEntity.STAGE_2_TICKS /
 * STAGE_3_TICKS), and the third-person arm is raised like a trident throw.
 *
 * HAND-MAINTAINED: locked code element, registered through
 * EntityRenderersEvent.RegisterRenderers and RegisterClientExtensionsEvent below.
 */
@EventBusSubscriber(Dist.CLIENT)
public class ChainHookRenderer extends EntityRenderer<ChainHookEntity, ChainHookRenderer.State> {
	private static final Identifier HEAD_TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/chain_hook_head.png");
	/** Vanilla's iron chain item sprite. Referenced, not copied, so resource packs restyle the chain too. */
	private static final Identifier CHAIN_TEXTURE = Identifier.withDefaultNamespace("textures/item/iron_chain.png");
	private static final RenderType HEAD_RENDER_TYPE = RenderTypes.entityCutout(HEAD_TEXTURE);
	private static final RenderType CHAIN_RENDER_TYPE = RenderTypes.entityCutout(CHAIN_TEXTURE);
	private static final float HEAD_SIZE = 0.5F;
	private static final float HEAD_LIFT = 0.125F;
	/**
	 * The repeating piece of the sprite: pixel columns 6-8 and rows 2-6 hold one round link
	 * and one side-on link, and row 7 repeats row 2, so this 3x5 pixel area tiles seamlessly.
	 * CHAIN_MASK marks its opaque pixels ([row][column]), copied from the vanilla sprite. A
	 * resource pack that redraws the chain keeps its colours here but not its pixel layout.
	 */
	private static final int CHAIN_COL0 = 6;
	private static final int CHAIN_ROW0 = 2;
	private static final boolean[][] CHAIN_MASK = {
			{true, true, true},
			{false, true, false},
			{true, true, true},
			{true, false, true},
			{true, false, true}};
	/** One sprite pixel in the world, at vanilla scale. */
	private static final double PIXEL = 1.0 / 16.0;
	private static final double LINK_TILE_LENGTH = CHAIN_MASK.length * PIXEL;
	/** First-person spin speed per stage, in degrees per tick (18 = one turn per second). */
	private static final float[] SPIN_DEGREES_PER_TICK = {18.0F, 36.0F, 72.0F};
	/** Point the held item whirls around, relative to vanilla's hand position. */
	private static final float SPIN_PIVOT_Y = 0.0F;
	private static final float SPIN_PIVOT_Z = 0.0F;

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ChainHookEntity.TYPE.get(), ChainHookRenderer::new);
	}

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(new ItemExtensions(), WitchercraftModItems.CHAIN_HOOK.get());
	}

	public ChainHookRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public static class State extends EntityRenderState {
		/** Owner's hand minus the chain start, in world units. */
		public Vec3 chainOffset = Vec3.ZERO;
		public boolean hasOwner;
		public boolean hooked;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public boolean shouldRender(ChainHookEntity entity, Frustum culler, double camX, double camY, double camZ) {
		return super.shouldRender(entity, culler, camX, camY, camZ) && entity.getPlayerOwner() != null;
	}

	@Override
	protected boolean affectedByCulling(ChainHookEntity entity) {
		return false;
	}

	@Override
	public void extractRenderState(ChainHookEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.hooked = entity.getHookedIn() != null;
		Player owner = entity.getPlayerOwner();
		state.hasOwner = owner != null;
		if (owner == null) {
			state.chainOffset = Vec3.ZERO;
			return;
		}
		float swing = Mth.sin(Mth.sqrt(owner.getAttackAnim(partialTicks)) * (float) Math.PI);
		Vec3 hand = this.getPlayerHandPos(owner, swing, partialTicks);
		Vec3 start = entity.getPosition(partialTicks).add(0.0, HEAD_LIFT, 0.0);
		state.chainOffset = hand.subtract(start);
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.0F, HEAD_LIFT, 0.0F);
		if (!state.hooked) {
			poseStack.pushPose();
			poseStack.scale(HEAD_SIZE, HEAD_SIZE, HEAD_SIZE);
			poseStack.mulPose(camera.orientation);
			collector.submitCustomGeometry(poseStack, HEAD_RENDER_TYPE, (pose, buffer) -> {
				headVertex(buffer, pose, state.lightCoords, 0.0F, 0, 0, 1);
				headVertex(buffer, pose, state.lightCoords, 1.0F, 0, 1, 1);
				headVertex(buffer, pose, state.lightCoords, 1.0F, 1, 1, 0);
				headVertex(buffer, pose, state.lightCoords, 0.0F, 1, 0, 0);
			});
			poseStack.popPose();
		}
		if (state.hasOwner)
			submitChain(state, poseStack, collector, camera);
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}

	private static void submitChain(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		Vec3 offset = state.chainOffset;
		double length = offset.length();
		if (length < 1.0E-3)
			return;
		Vec3 dir = offset.scale(1.0 / length);
		// Camera position in the same local space as the chain (origin = chain start).
		Vec3 eye = camera.pos.subtract(state.x, state.y + HEAD_LIFT, state.z);
		int light = state.lightCoords;
		int rows = CHAIN_MASK.length;
		int cols = CHAIN_MASK[0].length;
		collector.submitCustomGeometry(poseStack, CHAIN_RENDER_TYPE, (pose, buffer) -> {
			for (double a = 0.0; a < length; a += LINK_TILE_LENGTH) {
				// Turn this tile around the chain axis so its face points at the camera.
				Vec3 toEye = eye.subtract(dir.scale(a + LINK_TILE_LENGTH * 0.5));
				Vec3 cross = dir.cross(toEye);
				if (cross.lengthSqr() < 1.0E-8)
					continue;
				Vec3 side = cross.normalize();
				Vec3 face = side.cross(dir);
				for (int r = 0; r < rows; r++) {
					double r0 = a + r * PIXEL;
					if (r0 >= length)
						break;
					double r1 = Math.min(r0 + PIXEL, length);
					float v = (CHAIN_ROW0 + r + 0.5F) / 16.0F;
					for (int c = 0; c < cols; c++) {
						if (!CHAIN_MASK[r][c])
							continue;
						double c0 = (c - cols / 2.0) * PIXEL;
						float u = (CHAIN_COL0 + c + 0.5F) / 16.0F;
						// Faces shared with an opaque neighbour are inside the chain; skip them.
						boolean prev = CHAIN_MASK[(r + rows - 1) % rows][c];
						boolean next = CHAIN_MASK[(r + 1) % rows][c];
						boolean left = c > 0 && CHAIN_MASK[r][c - 1];
						boolean right = c < cols - 1 && CHAIN_MASK[r][c + 1];
						voxel(buffer, pose, light, dir, side, face, r0, r1, c0, c0 + PIXEL, u, v, !prev, !next || r1 >= length, !left, !right);
					}
				}
			}
		});
	}

	/**
	 * One sprite pixel as a 1-pixel-deep cube spanning [d0,d1] along {@code dir} and [s0,s1]
	 * along {@code side}, centred on the strip plane. Every face samples the pixel centre, so the
	 * cube takes that pixel's colour, like a vanilla item's extruded edges.
	 */
	private static void voxel(VertexConsumer buffer, PoseStack.Pose pose, int light, Vec3 dir, Vec3 side, Vec3 face, double d0, double d1, double s0, double s1, float u, float v, boolean start,
			boolean end, boolean low, boolean high) {
		double h = PIXEL / 2.0;
		Vec3[] p = new Vec3[8];
		for (int i = 0; i < 8; i++)
			p[i] = dir.scale((i & 1) == 0 ? d0 : d1).add(side.scale((i & 2) == 0 ? s0 : s1)).add(face.scale((i & 4) == 0 ? -h : h));
		quad(buffer, pose, light, face, p[4], p[5], p[7], p[6], u, v);
		quad(buffer, pose, light, face.scale(-1.0), p[0], p[2], p[3], p[1], u, v);
		if (start)
			quad(buffer, pose, light, dir.scale(-1.0), p[0], p[4], p[6], p[2], u, v);
		if (end)
			quad(buffer, pose, light, dir, p[1], p[3], p[7], p[5], u, v);
		if (low)
			quad(buffer, pose, light, side.scale(-1.0), p[0], p[1], p[5], p[4], u, v);
		if (high)
			quad(buffer, pose, light, side, p[2], p[6], p[7], p[3], u, v);
	}

	private static void quad(VertexConsumer buffer, PoseStack.Pose pose, int light, Vec3 normal, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float u, float v) {
		chainVertex(buffer, pose, light, a, normal, u, v);
		chainVertex(buffer, pose, light, b, normal, u, v);
		chainVertex(buffer, pose, light, c, normal, u, v);
		chainVertex(buffer, pose, light, d, normal, u, v);
	}

	private static void chainVertex(VertexConsumer buffer, PoseStack.Pose pose, int light, Vec3 pos, Vec3 normal, float u, float v) {
		buffer.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, (float) normal.x, (float) normal.y,
				(float) normal.z);
	}

	private static void headVertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, int y, int u, int v) {
		buffer.addVertex(pose, x - 0.5F, y - 0.5F, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private static HumanoidArm getHoldingArm(Player owner) {
		return owner.getMainHandItem().is(WitchercraftModItems.CHAIN_HOOK.get()) ? owner.getMainArm() : owner.getMainArm().getOpposite();
	}

	private Vec3 getPlayerHandPos(Player owner, float swing, float partialTicks) {
		int invert = getHoldingArm(owner) == HumanoidArm.RIGHT ? 1 : -1;
		if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && owner == Minecraft.getInstance().player) {
			float fov = this.entityRenderDispatcher.options.fov().get().intValue();
			double viewBobbingScale = 960.0 / fov;
			Vec3 viewVec = this.entityRenderDispatcher.camera.getNearPlane(fov).getPointOnPlane(invert * 0.525F, -0.1F).scale(viewBobbingScale).yRot(swing * 0.5F).xRot(-swing * 0.7F);
			return owner.getEyePosition(partialTicks).add(viewVec);
		}
		float ownerYRot = Mth.lerp(partialTicks, owner.yBodyRotO, owner.yBodyRot) * (float) (Math.PI / 180.0);
		double sin = Mth.sin(ownerYRot);
		double cos = Mth.cos(ownerYRot);
		float playerScale = owner.getScale();
		double rightOffset = invert * 0.35 * playerScale;
		double forwardOffset = 0.8 * playerScale;
		float yOffset = owner.isCrouching() ? -0.1875F : 0.0F;
		return owner.getEyePosition(partialTicks).add(-cos * rightOffset - sin * forwardOffset, yOffset - 0.45 * playerScale, -sin * rightOffset + cos * forwardOffset);
	}

	/** Total spin angle after {@code ticks} of charging, accumulated stage by stage so speed changes never jump. */
	private static float spinAngle(float ticks) {
		float s2 = ChainHookEntity.STAGE_2_TICKS;
		float s3 = ChainHookEntity.STAGE_3_TICKS;
		if (ticks < s2)
			return ticks * SPIN_DEGREES_PER_TICK[0];
		if (ticks < s3)
			return s2 * SPIN_DEGREES_PER_TICK[0] + (ticks - s2) * SPIN_DEGREES_PER_TICK[1];
		return s2 * SPIN_DEGREES_PER_TICK[0] + (s3 - s2) * SPIN_DEGREES_PER_TICK[1] + (ticks - s3) * SPIN_DEGREES_PER_TICK[2];
	}

	private static boolean isCharging(LivingEntity entity, InteractionHand hand) {
		return entity.isUsingItem() && entity.getUsedItemHand() == hand && entity.getUseItem().is(WitchercraftModItems.CHAIN_HOOK.get());
	}

	private static class ItemExtensions implements IClientItemExtensions {
		@Override
		public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack itemStack) {
			return isCharging(entity, hand) ? HumanoidModel.ArmPose.THROW_TRIDENT : null;
		}

		@Override
		public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
			InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			if (!isCharging(player, hand))
				return false;
			int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
			poseStack.translate(invert * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
			// Whirl around the left-right axis (pitch): forward underneath, back over the top.
			poseStack.translate(0.0F, SPIN_PIVOT_Y, SPIN_PIVOT_Z);
			poseStack.mulPose(Axis.XP.rotationDegrees(spinAngle(player.getTicksUsingItem() + partialTick)));
			poseStack.translate(0.0F, -SPIN_PIVOT_Y, -SPIN_PIVOT_Z);
			return true;
		}
	}
}
