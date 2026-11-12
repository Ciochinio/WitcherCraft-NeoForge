package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Draws the Active Shield (alt Quen) bubble for each player WitchercraftQuenBubbleMessage reports as
 * shielded: a low-poly geodesic shell (icosahedron split once, 80 faces) with glowing frame edges and
 * faintly filled faces. Faces brighten towards the silhouette (rim glow), each face shimmers on its own
 * slow cycle, the shell turns slowly, the whole thing brightens as the pool grows and flashes on every
 * absorbed hit. In first person your own shell is hidden and the screen edges glow instead; the edge
 * facing the attacker flashes on a hit. All tuning lives in the constants below.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftQuenBubbleClient {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/quen_bubble.png");
	private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
	private static final int FULL_BRIGHT = 0xF000F0;

	/** Centred low on the body so its bottom sinks into the ground. */
	private static final float RADIUS = 1.4F;
	/** Centre height as a share of the player's height. Hit particles in WitchercraftQuenBubbleHit mirror both. */
	private static final float CENTRE_HEIGHT = 0.5F;
	private static final int RED = 255;
	private static final int GREEN = 185;
	private static final int BLUE = 60;
	/** Face fill opacity facing the camera and at the silhouette (the render type discards anything under 0.1). */
	private static final float ALPHA_CENTER = 0.14F;
	private static final float ALPHA_RIM = 0.6F;
	/** Frame edges: opacity (0 = no frame), and how far in from each corner the face fill starts (1 = no frame). */
	private static final float EDGE_ALPHA = 0.0F;
	private static final float FACE_INSET = 1.0F;
	/** Pool at which the shell reaches full brightness, and the brightness share it starts at. */
	private static final float FULL_POOL = 16.0F;
	private static final float MIN_STRENGTH = 0.5F;
	/** Per-face shimmer: amount and speed (radians per tick). */
	private static final float SHIMMER = 0.35F;
	private static final float SHIMMER_SPEED = 0.12F;
	/** Slow turn of the whole shell, degrees per tick. */
	private static final float TURN_SPEED = 0.6F;
	/** Hit flash: extra brightness at the moment of the hit, and how long it takes to die out. */
	private static final float FLASH_BOOST = 1.5F;
	private static final float FLASH_TICKS = 6.0F;

	/** First-person screen-edge glow: depth as a share of screen height, opacity, and hit flash opacity. */
	private static final float EDGE_GLOW_DEPTH = 0.16F;
	private static final float EDGE_GLOW_ALPHA = 0.3F;
	private static final float EDGE_GLOW_FLASH = 0.5F;

	/** Face corners of the geodesic shell on the unit sphere, 3 x 3 floats per face. */
	private static final List<float[][]> FACES = buildGeodesic();

	@SubscribeEvent
	public static void onSubmit(SubmitCustomGeometryEvent event) {
		Map<Integer, WitchercraftQuenBubbleMessage.Bubble> bubbles = WitchercraftQuenBubbleMessage.BUBBLES;
		Minecraft mc = Minecraft.getInstance();
		if (bubbles.isEmpty() || mc.level == null)
			return;
		float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		long gameTime = mc.level.getGameTime();
		float now = gameTime + partial;
		Vec3 cam = event.getLevelRenderState().cameraRenderState.pos;
		Iterator<Map.Entry<Integer, WitchercraftQuenBubbleMessage.Bubble>> it = bubbles.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, WitchercraftQuenBubbleMessage.Bubble> entry = it.next();
			WitchercraftQuenBubbleMessage.Bubble bubble = entry.getValue();
			if (bubble.up && gameTime - bubble.lastHeard > WitchercraftQuenBubbleMessage.STALE_TICKS)
				bubble.setUp(false, gameTime);
			if (!bubble.up && bubble.fade(now) <= 0.0F) {
				it.remove();
				continue;
			}
			Entity entity = mc.level.getEntity(entry.getKey());
			if (entity == null || (entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()))
				continue;
			float alpha = strength(bubble, now);
			if (alpha <= 0.0F)
				continue;
			Vec3 centre = entity.getPosition(partial).add(0.0, entity.getBbHeight() * CENTRE_HEIGHT, 0.0);
			float turn = now * TURN_SPEED * Mth.DEG_TO_RAD;
			// Camera in the shell's own turned frame, for the per-face rim glow.
			Vec3 eye = cam.subtract(centre).yRot(-turn);
			PoseStack poseStack = event.getPoseStack();
			poseStack.pushPose();
			poseStack.translate(centre.x - cam.x, centre.y - cam.y, centre.z - cam.z);
			poseStack.mulPose(com.mojang.math.Axis.YP.rotation(turn));
			event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> shell(buffer, pose, eye, alpha, now));
			poseStack.popPose();
		}
	}

	/** Fade x pool brightness x hit flash. */
	private static float strength(WitchercraftQuenBubbleMessage.Bubble bubble, float now) {
		float pool = MIN_STRENGTH + (1.0F - MIN_STRENGTH) * Mth.clamp(bubble.pool / FULL_POOL, 0.0F, 1.0F);
		return bubble.fade(now) * pool * (1.0F + FLASH_BOOST * flash(bubble, now));
	}

	private static float flash(WitchercraftQuenBubbleMessage.Bubble bubble, float now) {
		return Mth.clamp(1.0F - (now - bubble.hitTime) / FLASH_TICKS, 0.0F, 1.0F);
	}

	private static void shell(VertexConsumer buffer, PoseStack.Pose pose, Vec3 eye, float alpha, float now) {
		for (int f = 0; f < FACES.size(); f++) {
			float[][] c = FACES.get(f);
			float cx = (c[0][0] + c[1][0] + c[2][0]) / 3.0F;
			float cy = (c[0][1] + c[1][1] + c[2][1]) / 3.0F;
			float cz = (c[0][2] + c[1][2] + c[2][2]) / 3.0F;
			float nl = Mth.sqrt(cx * cx + cy * cy + cz * cz);
			float nx = cx / nl, ny = cy / nl, nz = cz / nl;
			Vec3 toEye = eye.subtract(cx * RADIUS, cy * RADIUS, cz * RADIUS);
			double len = toEye.length();
			float facing = len < 1.0E-4 ? 1.0F : (float) Math.abs((nx * toEye.x + ny * toEye.y + nz * toEye.z) / len);
			float edge = (1.0F - facing) * (1.0F - facing);
			float shimmer = 1.0F + SHIMMER * Mth.sin(now * SHIMMER_SPEED + f * 2.399F);
			int fillA = alphaByte((ALPHA_CENTER + (ALPHA_RIM - ALPHA_CENTER) * edge) * alpha * shimmer);
			int edgeA = alphaByte(EDGE_ALPHA * alpha * shimmer);
			float[][] p = new float[3][3];
			float[][] q = new float[3][3];
			for (int i = 0; i < 3; i++) {
				for (int k = 0; k < 3; k++)
					p[i][k] = c[i][k] * RADIUS;
				q[i][0] = (cx + (c[i][0] - cx) * FACE_INSET) * RADIUS;
				q[i][1] = (cy + (c[i][1] - cy) * FACE_INSET) * RADIUS;
				q[i][2] = (cz + (c[i][2] - cz) * FACE_INSET) * RADIUS;
			}
			// The fill as a quad with its last corner doubled (the render type draws quads).
			vertex(buffer, pose, q[0], nx, ny, nz, fillA);
			vertex(buffer, pose, q[1], nx, ny, nz, fillA);
			vertex(buffer, pose, q[2], nx, ny, nz, fillA);
			vertex(buffer, pose, q[2], nx, ny, nz, fillA);
			if (EDGE_ALPHA <= 0.0F || FACE_INSET >= 1.0F)
				continue;
			for (int i = 0; i < 3; i++) {
				int j = (i + 1) % 3;
				vertex(buffer, pose, p[i], nx, ny, nz, edgeA);
				vertex(buffer, pose, p[j], nx, ny, nz, edgeA);
				vertex(buffer, pose, q[j], nx, ny, nz, edgeA);
				vertex(buffer, pose, q[i], nx, ny, nz, edgeA);
			}
		}
	}

	private static int alphaByte(float a) {
		return (int) (Mth.clamp(a, 0.0F, 1.0F) * 255.0F);
	}

	private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float[] p, float nx, float ny, float nz, int alpha) {
		buffer.addVertex(pose, p[0], p[1], p[2]).setColor(RED, GREEN, BLUE, alpha).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, ny, nz);
	}

	@SubscribeEvent
	public static void onRenderGui(RenderGuiLayerEvent.Post event) {
		if (!event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS))
			return;
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.level == null || mc.options.hideGui || !mc.options.getCameraType().isFirstPerson() || mc.getCameraEntity() != player)
			return;
		WitchercraftQuenBubbleMessage.Bubble bubble = WitchercraftQuenBubbleMessage.BUBBLES.get(player.getId());
		if (bubble == null)
			return;
		float now = mc.level.getGameTime() + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float fade = bubble.fade(now);
		if (fade <= 0.0F)
			return;
		float base = EDGE_GLOW_ALPHA * fade * (MIN_STRENGTH + (1.0F - MIN_STRENGTH) * Mth.clamp(bubble.pool / FULL_POOL, 0.0F, 1.0F));
		float flash = EDGE_GLOW_FLASH * flash(bubble, now) * fade;
		// Where the hit came from, relative to where you face: front = top edge, behind = bottom.
		float top = 1.0F, bottom = 1.0F, left = 1.0F, right = 1.0F;
		double dl = Math.sqrt(bubble.hitDx * bubble.hitDx + bubble.hitDz * bubble.hitDz);
		if (dl > 1.0E-4) {
			float yaw = player.getYRot(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false)) * Mth.DEG_TO_RAD;
			double dx = bubble.hitDx / dl, dz = bubble.hitDz / dl;
			double fwd = dx * -Mth.sin(yaw) + dz * Mth.cos(yaw);
			double side = dx * -Mth.cos(yaw) + dz * -Mth.sin(yaw);
			top = (float) Math.max(0.0, fwd);
			bottom = (float) Math.max(0.0, -fwd);
			right = (float) Math.max(0.0, side);
			left = (float) Math.max(0.0, -side);
		}
		GuiGraphicsExtractor g = event.getGuiGraphics();
		int w = g.guiWidth(), h = g.guiHeight();
		int depth = Math.max(1, Math.round(h * EDGE_GLOW_DEPTH));
		for (int i = 0; i < depth; i++) {
			float fall = 1.0F - (float) i / depth;
			fall *= fall;
			g.fill(0, i, w, i + 1, argb((base + flash * top) * fall));
			g.fill(0, h - i - 1, w, h - i, argb((base + flash * bottom) * fall));
			g.fill(i, 0, i + 1, h, argb((base + flash * left) * fall));
			g.fill(w - i - 1, 0, w - i, h, argb((base + flash * right) * fall));
		}
	}

	private static int argb(float a) {
		return (alphaByte(a) << 24) | (RED << 16) | (GREEN << 8) | BLUE;
	}

	/** Icosahedron with every face split into 4, corners pushed out to the unit sphere: 80 faces. */
	private static List<float[][]> buildGeodesic() {
		float t = (1.0F + Mth.sqrt(5.0F)) / 2.0F;
		float[][] v = {{-1, t, 0}, {1, t, 0}, {-1, -t, 0}, {1, -t, 0}, {0, -1, t}, {0, 1, t}, {0, -1, -t}, {0, 1, -t}, {t, 0, -1}, {t, 0, 1}, {-t, 0, -1}, {-t, 0, 1}};
		int[][] ico = {{0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11}, {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8}, {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9}, {4, 9, 5},
				{2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}};
		List<float[][]> out = new ArrayList<>();
		for (int[] f : ico) {
			float[] a = unit(v[f[0]]), b = unit(v[f[1]]), c = unit(v[f[2]]);
			float[] ab = unit(mid(a, b)), bc = unit(mid(b, c)), ca = unit(mid(c, a));
			out.add(new float[][]{a, ab, ca});
			out.add(new float[][]{ab, b, bc});
			out.add(new float[][]{ca, bc, c});
			out.add(new float[][]{ab, bc, ca});
		}
		return out;
	}

	private static float[] mid(float[] a, float[] b) {
		return new float[]{(a[0] + b[0]) / 2.0F, (a[1] + b[1]) / 2.0F, (a[2] + b[2]) / 2.0F};
	}

	private static float[] unit(float[] a) {
		float l = Mth.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
		return new float[]{a[0] / l, a[1] / l, a[2] / l};
	}
}
