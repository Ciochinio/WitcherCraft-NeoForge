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

/**
 * Draws the Active Shield (alt Quen) bubble for each player WitchercraftQuenBubbleMessage reports as
 * shielded: a smooth sphere made of quads (rings x slices) wrapped in a tiled pixel-glass texture
 * (textures/entities/quen_bubble_glass.png, tinted amber). It brightens towards the silhouette (rim glow),
 * turns in steps of exactly one texture pixel so the glass slides pixel by pixel, brightens as the pool grows
 * and flashes as a whole on every absorbed hit. Quen symbols (textures/entities/quen_bubble_symbol.png) glow on the
 * surface and turn, pulse and flash with it. In first person your own bubble is hidden and the screen edges
 * glow instead; the edge facing the attacker flashes on a hit. All tuning lives in the constants below.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftQuenBubbleClient {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/quen_bubble_glass.png");
	private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
	/** Glow layer: the spider-eyes render type adds its colour to the screen, so what's behind gets brighter, not tinted. */
	private static final RenderType GLOW_RENDER_TYPE = RenderTypes.eyes(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/quen_bubble.png"));
	/** Quen symbols on the surface, additive like the glow layer, so the PNG's clear pixels must stay black. */
	private static final RenderType SYMBOL_RENDER_TYPE = RenderTypes.eyes(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/quen_bubble_symbol.png"));
	private static final int FULL_BRIGHT = 0xF000F0;

	/** Centred low on the body so its bottom sinks into the ground. */
	private static final float RADIUS = 1.4F;
	/** Centre height as a share of the player's height. Hit particles in WitchercraftQuenBubbleHit mirror both. */
	private static final float CENTRE_HEIGHT = 0.5F;
	private static final int RED = 255;
	private static final int GREEN = 215;
	private static final int BLUE = 140;
	/**
	 * Opacity facing the camera and at the silhouette. Multiplied by the texture's own alpha, and the render type
	 * discards anything under 0.1, so at a low pool only the lighter patches and glints of the pane survive in the
	 * middle and the glass fills in as the pool grows.
	 */
	private static final float ALPHA_CENTER = 0.6F;
	private static final float ALPHA_RIM = 0.95F;
	/** Pool at which the bubble reaches full brightness, and the brightness share it starts at. */
	private static final float FULL_POOL = 16.0F;
	private static final float MIN_STRENGTH = 0.5F;
	/** Sphere detail: slices around and rings top to bottom. Must divide evenly by the tile counts below. */
	private static final int SLICES = 24;
	private static final int RINGS = 16;
	/** How many times the glass tile repeats around and top to bottom (about block-sized pixels at RADIUS 1.4). */
	private static final int TILES_AROUND = 4;
	private static final int TILES_DOWN = 2;
	private static final int TEXTURE_PIXELS = 32;
	/** Turn: one texture pixel every this many ticks. */
	private static final float TURN_STEP_TICKS = 5.0F;
	/** Glow layer: size relative to the glass, and brightness facing the camera and at the silhouette. */
	private static final float GLOW_SCALE = 0.97F;
	private static final float GLOW_CENTER = 0.12F;
	private static final float GLOW_RIM = 0.65F;
	/** Glow pulse: how much the brightness swings either way, and ticks per breath. */
	private static final float PULSE = 0.15F;
	private static final float PULSE_TICKS = 40.0F;
	/** Hit flash: extra brightness at the moment of the hit, and how long it takes to die out. */
	private static final float FLASH_BOOST = 1.5F;
	private static final float FLASH_TICKS = 6.0F;
	/**
	 * Symbols: how many (spread evenly over the whole sphere), width in blocks, how far outside the glass they sit,
	 * and brightness. Only the side facing the camera is drawn, fading out towards the silhouette.
	 */
	private static final int SYMBOL_COUNT = 20;
	private static final float SYMBOL_SIZE = 0.3F;
	private static final float SYMBOL_LIFT = 1.01F;
	private static final float SYMBOL_BRIGHTNESS = 0.9F;

	/** First-person screen-edge glow: depth as a share of screen height, opacity, and hit flash opacity. */
	private static final float EDGE_GLOW_DEPTH = 0.16F;
	private static final float EDGE_GLOW_ALPHA = 0.3F;
	private static final float EDGE_GLOW_FLASH = 0.5F;

	/** Grid points of the unit sphere, [ring][slice][xyz], ring 0 at the top. */
	private static final float[][][] GRID = buildSphere();
	/** Per symbol: [centre direction, right, up], each a unit xyz on the unit sphere. */
	private static final float[][][] SYMBOLS = buildSymbols();

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
			float turn = (float) Math.floor(now / TURN_STEP_TICKS) * (Mth.TWO_PI / (TILES_AROUND * TEXTURE_PIXELS));
			// Camera in the bubble's own turned frame, for the rim glow.
			Vec3 eye = cam.subtract(centre).yRot(-turn);
			PoseStack poseStack = event.getPoseStack();
			poseStack.pushPose();
			poseStack.translate(centre.x - cam.x, centre.y - cam.y, centre.z - cam.z);
			poseStack.mulPose(com.mojang.math.Axis.YP.rotation(turn));
			event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> shell(buffer, pose, eye, alpha));
			float glow = alpha * (1.0F + PULSE * Mth.sin(now * Mth.TWO_PI / PULSE_TICKS));
			event.getSubmitNodeCollector().submitCustomGeometry(poseStack, GLOW_RENDER_TYPE, (pose, buffer) -> glow(buffer, pose, eye, glow));
			event.getSubmitNodeCollector().submitCustomGeometry(poseStack, SYMBOL_RENDER_TYPE, (pose, buffer) -> symbols(buffer, pose, eye, glow));
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

	private static void shell(VertexConsumer buffer, PoseStack.Pose pose, Vec3 eye, float alpha) {
		int[][] a = new int[RINGS + 1][SLICES + 1];
		for (int r = 0; r <= RINGS; r++) {
			for (int s = 0; s <= SLICES; s++) {
				float[] n = GRID[r][s];
				Vec3 toEye = eye.subtract(n[0] * RADIUS, n[1] * RADIUS, n[2] * RADIUS);
				double len = toEye.length();
				float facing = len < 1.0E-4 ? 1.0F : (float) Math.abs((n[0] * toEye.x + n[1] * toEye.y + n[2] * toEye.z) / len);
				float edge = (1.0F - facing) * (1.0F - facing);
				a[r][s] = alphaByte((ALPHA_CENTER + (ALPHA_RIM - ALPHA_CENTER) * edge) * alpha);
			}
		}
		// Each quad gets its own slice of one tile (UVs stay inside 0..1), so no texture repeat is needed.
		int slicesPerTile = SLICES / TILES_AROUND, ringsPerTile = RINGS / TILES_DOWN;
		for (int r = 0; r < RINGS; r++) {
			float v0 = (float) (r % ringsPerTile) / ringsPerTile, v1 = (float) (r % ringsPerTile + 1) / ringsPerTile;
			for (int s = 0; s < SLICES; s++) {
				float u0 = (float) (s % slicesPerTile) / slicesPerTile, u1 = (float) (s % slicesPerTile + 1) / slicesPerTile;
				vertex(buffer, pose, GRID[r][s], u0, v0, a[r][s]);
				vertex(buffer, pose, GRID[r + 1][s], u0, v1, a[r + 1][s]);
				vertex(buffer, pose, GRID[r + 1][s + 1], u1, v1, a[r + 1][s + 1]);
				vertex(buffer, pose, GRID[r][s + 1], u1, v0, a[r][s + 1]);
			}
		}
	}

	/** Additive: brightness is carried in the colour, so it's scaled into RGB rather than alpha. */
	private static void glow(VertexConsumer buffer, PoseStack.Pose pose, Vec3 eye, float strength) {
		float[][] b = new float[RINGS + 1][SLICES + 1];
		float radius = RADIUS * GLOW_SCALE;
		for (int r = 0; r <= RINGS; r++) {
			for (int s = 0; s <= SLICES; s++) {
				float[] n = GRID[r][s];
				Vec3 toEye = eye.subtract(n[0] * radius, n[1] * radius, n[2] * radius);
				double len = toEye.length();
				float facing = len < 1.0E-4 ? 1.0F : (float) Math.abs((n[0] * toEye.x + n[1] * toEye.y + n[2] * toEye.z) / len);
				float edge = (1.0F - facing) * (1.0F - facing);
				b[r][s] = Mth.clamp((GLOW_CENTER + (GLOW_RIM - GLOW_CENTER) * edge) * strength, 0.0F, 1.0F);
			}
		}
		for (int r = 0; r < RINGS; r++) {
			for (int s = 0; s < SLICES; s++) {
				glowVertex(buffer, pose, GRID[r][s], radius, 0, 0, b[r][s]);
				glowVertex(buffer, pose, GRID[r + 1][s], radius, 0, 1, b[r + 1][s]);
				glowVertex(buffer, pose, GRID[r + 1][s + 1], radius, 1, 1, b[r + 1][s + 1]);
				glowVertex(buffer, pose, GRID[r][s + 1], radius, 1, 0, b[r][s + 1]);
			}
		}
	}

	private static void symbols(VertexConsumer buffer, PoseStack.Pose pose, Vec3 eye, float strength) {
		float centre = RADIUS * SYMBOL_LIFT, half = SYMBOL_SIZE / 2.0F;
		for (float[][] sym : SYMBOLS) {
			float[] n = sym[0], r = sym[1], u = sym[2];
			Vec3 toEye = eye.subtract(n[0] * centre, n[1] * centre, n[2] * centre);
			double len = toEye.length();
			float facing = len < 1.0E-4 ? 0.0F : (float) ((n[0] * toEye.x + n[1] * toEye.y + n[2] * toEye.z) / len);
			if (facing <= 0.0F)
				continue;
			float b = Mth.clamp(SYMBOL_BRIGHTNESS * strength * facing, 0.0F, 1.0F);
			int c = (int) (255 * b);
			float cx = n[0] * centre, cy = n[1] * centre, cz = n[2] * centre;
			symbolVertex(buffer, pose, cx - (r[0] + u[0]) * half, cy - (r[1] + u[1]) * half, cz - (r[2] + u[2]) * half, 0, 1, c, n);
			symbolVertex(buffer, pose, cx + (r[0] - u[0]) * half, cy + (r[1] - u[1]) * half, cz + (r[2] - u[2]) * half, 1, 1, c, n);
			symbolVertex(buffer, pose, cx + (r[0] + u[0]) * half, cy + (r[1] + u[1]) * half, cz + (r[2] + u[2]) * half, 1, 0, c, n);
			symbolVertex(buffer, pose, cx - (r[0] - u[0]) * half, cy - (r[1] - u[1]) * half, cz - (r[2] - u[2]) * half, 0, 0, c, n);
		}
	}

	private static void symbolVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int c, float[] n) {
		buffer.addVertex(pose, x, y, z).setColor(c, c, c, c).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, n[0], n[1], n[2]);
	}

	/** Golden-angle spiral: even spacing over the sphere with no rows and no bunching at the poles. Each symbol stands upright (its top towards the bubble's top). */
	private static float[][][] buildSymbols() {
		float[][][] out = new float[SYMBOL_COUNT][][];
		float golden = (float) (Math.PI * (3.0 - Math.sqrt(5.0)));
		for (int i = 0; i < SYMBOL_COUNT; i++) {
			float y = 1.0F - 2.0F * (i + 0.5F) / SYMBOL_COUNT;
			float ring = Mth.sqrt(1.0F - y * y);
			float[] n = { Mth.cos(i * golden) * ring, y, Mth.sin(i * golden) * ring };
			// right = worldUp x n, up = n x right
			float rx = n[2], rz = -n[0];
			float rl = Mth.sqrt(rx * rx + rz * rz);
			float[] r = rl < 1.0E-4 ? new float[] { 1, 0, 0 } : new float[] { rx / rl, 0, rz / rl };
			float[] u = { n[1] * r[2] - n[2] * r[1], n[2] * r[0] - n[0] * r[2], n[0] * r[1] - n[1] * r[0] };
			out[i] = new float[][] { n, r, u };
		}
		return out;
	}

	private static void glowVertex(VertexConsumer buffer, PoseStack.Pose pose, float[] n, float radius, float u, float v, float b) {
		buffer.addVertex(pose, n[0] * radius, n[1] * radius, n[2] * radius).setColor((int) (RED * b), (int) (GREEN * b), (int) (BLUE * b), alphaByte(b)).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, n[0], n[1], n[2]);
	}

	private static int alphaByte(float a) {
		return (int) (Mth.clamp(a, 0.0F, 1.0F) * 255.0F);
	}

	private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float[] n, float u, float v, int alpha) {
		buffer.addVertex(pose, n[0] * RADIUS, n[1] * RADIUS, n[2] * RADIUS).setColor(RED, GREEN, BLUE, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, n[0], n[1], n[2]);
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

	private static float[][][] buildSphere() {
		float[][][] g = new float[RINGS + 1][SLICES + 1][];
		for (int r = 0; r <= RINGS; r++) {
			float lat = Mth.PI * r / RINGS;
			for (int s = 0; s <= SLICES; s++) {
				float lon = Mth.TWO_PI * s / SLICES;
				g[r][s] = new float[]{Mth.sin(lat) * Mth.cos(lon), Mth.cos(lat), Mth.sin(lat) * Mth.sin(lon)};
			}
		}
		return g;
	}
}
