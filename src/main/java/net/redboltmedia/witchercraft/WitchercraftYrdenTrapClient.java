package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Iterator;
import java.util.Map;

/**
 * Draws every Yrden trap WitchercraftYrdenTrapMessage reports: at each of the 7 glyph spots (the original
 * Yrden layout, offsets from the cast position) a soft purple pillar of light rising from the ground and
 * the Yrden glyph at its base (flat or upright, see GLYPH_FLAT), plus a purple tint on the ground across the
 * trap and motes rising from it. Each spot is
 * snapped to the top of the ground block in its own column, so slopes and steps work; a spot with no
 * ground near the trap's height is skipped. Fades in on cast, gone instantly when the trap ends.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftYrdenTrapClient {
	private static final Identifier GLYPH_TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/particle/yrden.png");
	private static final Identifier PILLAR_TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/yrden_pillar.png");
	private static final RenderType GLYPH_RENDER = RenderTypes.entityTranslucentEmissive(GLYPH_TEXTURE);
	private static final RenderType PILLAR_RENDER = RenderTypes.entityTranslucentEmissive(PILLAR_TEXTURE);
	private static final int FULL_BRIGHT = 0xF000F0;

	/** Glyph spots, x/z offsets from the cast position (the original Yrden layout). */
	private static final double[][] SPOTS = {{0, -4}, {-2.5, -2.5}, {-3, 0}, {-2, 2.5}, {2, 2.5}, {3, 0}, {2.5, -2.5}};
	/** How far above and below the trap's height a spot looks for ground. YrdenTrapTick catches mobs over the same range (7 below to 5 above). */
	private static final int GROUND_SEARCH_UP = 4;
	private static final int GROUND_SEARCH_DOWN = 6;
	/** Trap radius (YrdenTrapTick uses the same). */
	private static final double RADIUS = 3.5;

	private static final int RED = 175;
	private static final int GREEN = 90;
	private static final int BLUE = 255;
	private static final float PILLAR_HEIGHT = 1.6F;
	private static final float PILLAR_WIDTH = 0.6F;
	/** Pillar opacity at the ground; it fades to nothing at the top (the render type drops anything under 0.1). */
	private static final float PILLAR_ALPHA = 0.55F;
	/** true = glyphs lie flat on the ground; false = they stand upright and turn to face the camera. */
	private static final boolean GLYPH_FLAT = false;
	private static final float GLYPH_SIZE = 1.0F;
	private static final float GLYPH_ALPHA = 0.95F;
	/** Lift above the ground so a flat glyph doesn't flicker into the block top. */
	private static final float GLYPH_LIFT = 0.02F;
	private static final float PULSE = 0.15F;
	private static final float PULSE_SPEED = 0.12F;
	private static final float FADE_IN_TICKS = 4.0F;

	/** Ground glow: a purple tint on top of every block inside the trap, stronger near the edge. 0 = off. */
	private static final Identifier GROUND_TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/quen_bubble.png");
	private static final RenderType GROUND_RENDER = RenderTypes.entityTranslucentEmissive(GROUND_TEXTURE);
	private static final float GROUND_ALPHA = 0.14F;
	private static final float GROUND_EDGE_ALPHA = 0.16F;
	/** How far in from the edge the stronger edge tint reaches. */
	private static final double GROUND_EDGE_WIDTH = 1.0;
	private static final float GROUND_LIFT = 0.01F;
	/** Side faces down to lower ground: opacity as a share of the top tint (0 = off), and the most they reach down. */
	private static final float SIDE_ALPHA = 1.0F;
	private static final double SIDE_MAX_DROP = 3.0;

	/** Rising motes: particles per tick per trap from random ground spots inside it, and their upward speed. 0 = off. */
	private static final ParticleOptions MOTE = ParticleTypes.REVERSE_PORTAL;
	private static final int MOTES_PER_TICK = 2;
	private static final double MOTE_SPEED = 0.03;

	@SubscribeEvent
	public static void onSubmit(SubmitCustomGeometryEvent event) {
		Map<Integer, WitchercraftYrdenTrapMessage.Trap> traps = WitchercraftYrdenTrapMessage.TRAPS;
		Minecraft mc = Minecraft.getInstance();
		if (traps.isEmpty() || mc.level == null)
			return;
		long gameTime = mc.level.getGameTime();
		float now = gameTime + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		Vec3 cam = event.getLevelRenderState().cameraRenderState.pos;
		Iterator<WitchercraftYrdenTrapMessage.Trap> it = traps.values().iterator();
		while (it.hasNext()) {
			WitchercraftYrdenTrapMessage.Trap trap = it.next();
			if (gameTime - trap.lastHeard > WitchercraftYrdenTrapMessage.STALE_TICKS) {
				it.remove();
				continue;
			}
			float alpha = Mth.clamp((now - trap.upSince) / FADE_IN_TICKS, 0.0F, 1.0F);
			if (GROUND_ALPHA > 0.0F || GROUND_EDGE_ALPHA > 0.0F)
				ground(event, mc.level, trap, cam, alpha * (1.0F + PULSE * Mth.sin(now * PULSE_SPEED * 0.5F)));
			for (int i = 0; i < SPOTS.length; i++) {
				double sx = trap.x + SPOTS[i][0];
				double sz = trap.z + SPOTS[i][1];
				double gy = groundY(mc.level, sx, trap.y, sz);
				if (Double.isNaN(gy))
					continue;
				float pulse = alpha * (1.0F + PULSE * Mth.sin(now * PULSE_SPEED + i * 1.7F));
				double ox = SPOTS[i][0], oz = SPOTS[i][1];
				double ol = Math.sqrt(ox * ox + oz * oz);
				float outX = ol < 1.0E-4 ? 0 : (float) (ox / ol), outZ = ol < 1.0E-4 ? 1 : (float) (oz / ol);
				// Horizontal direction to the camera, for the pillar (and an upright glyph) to face it.
				double cx = cam.x - sx, cz = cam.z - sz, cl = Math.sqrt(cx * cx + cz * cz);
				float rightX = cl < 1.0E-4 ? 1 : (float) (cz / cl), rightZ = cl < 1.0E-4 ? 0 : (float) (-cx / cl);
				PoseStack poseStack = event.getPoseStack();
				poseStack.pushPose();
				poseStack.translate(sx - cam.x, gy - cam.y, sz - cam.z);
				event.getSubmitNodeCollector().submitCustomGeometry(poseStack, PILLAR_RENDER, (pose, buffer) -> pillar(buffer, pose, rightX, rightZ, pulse));
				event.getSubmitNodeCollector().submitCustomGeometry(poseStack, GLYPH_RENDER, (pose, buffer) -> glyph(buffer, pose, outX, outZ, rightX, rightZ, pulse));
				poseStack.popPose();
			}
		}
	}

	/**
	 * One flat tile on top of the ground in every block column whose centre is inside the trap, plus a tile on
	 * each side face that drops down to a lower neighbouring column (to its ground, or SIDE_MAX_DROP if it has
	 * none in range), so steps and slopes are tinted on their sides too.
	 */
	private static void ground(SubmitCustomGeometryEvent event, Level level, WitchercraftYrdenTrapMessage.Trap trap, Vec3 cam, float alpha) {
		int x0 = Mth.floor(trap.x - RADIUS), x1 = Mth.floor(trap.x + RADIUS);
		int z0 = Mth.floor(trap.z - RADIUS), z1 = Mth.floor(trap.z + RADIUS);
		// Ground heights for the trap's columns plus a one-block border, so every side can see its neighbour.
		int w = x1 - x0 + 3, l = z1 - z0 + 3;
		double[][] heights = new double[w][l];
		for (int i = 0; i < w; i++)
			for (int j = 0; j < l; j++)
				heights[i][j] = groundY(level, x0 - 1 + i + 0.5, trap.y, z0 - 1 + j + 0.5);
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(-cam.x, -cam.y, -cam.z);
		event.getSubmitNodeCollector().submitCustomGeometry(poseStack, GROUND_RENDER, (pose, buffer) -> {
			for (int bx = x0; bx <= x1; bx++) {
				for (int bz = z0; bz <= z1; bz++) {
					double dx = bx + 0.5 - trap.x, dz = bz + 0.5 - trap.z;
					double d = Math.sqrt(dx * dx + dz * dz);
					if (d > RADIUS)
						continue;
					int i = bx - x0 + 1, j = bz - z0 + 1;
					double gy = heights[i][j];
					if (Double.isNaN(gy))
						continue;
					float edge = (float) Mth.clamp((d - (RADIUS - GROUND_EDGE_WIDTH)) / GROUND_EDGE_WIDTH, 0.0, 1.0);
					int a = alphaByte((GROUND_ALPHA + GROUND_EDGE_ALPHA * edge) * alpha);
					float y = (float) gy + GROUND_LIFT;
					vertex(buffer, pose, bx, y, bz, 0, 0, RED, GREEN, BLUE, a);
					vertex(buffer, pose, bx, y, bz + 1, 0, 1, RED, GREEN, BLUE, a);
					vertex(buffer, pose, bx + 1, y, bz + 1, 1, 1, RED, GREEN, BLUE, a);
					vertex(buffer, pose, bx + 1, y, bz, 1, 0, RED, GREEN, BLUE, a);
					if (SIDE_ALPHA <= 0.0F)
						continue;
					int sa = alphaByte((GROUND_ALPHA + GROUND_EDGE_ALPHA * edge) * alpha * SIDE_ALPHA);
					float top = (float) gy;
					float o = GROUND_LIFT;
					float low;
					// West (-x), east (+x), north (-z), south (+z).
					if ((low = sideLow(heights[i - 1][j], gy)) < top) {
						vertex(buffer, pose, bx - o, low, bz, 0, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx - o, low, bz + 1, 1, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx - o, top, bz + 1, 1, 0, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx - o, top, bz, 0, 0, RED, GREEN, BLUE, sa);
					}
					if ((low = sideLow(heights[i + 1][j], gy)) < top) {
						vertex(buffer, pose, bx + 1 + o, low, bz, 0, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx + 1 + o, low, bz + 1, 1, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx + 1 + o, top, bz + 1, 1, 0, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx + 1 + o, top, bz, 0, 0, RED, GREEN, BLUE, sa);
					}
					if ((low = sideLow(heights[i][j - 1], gy)) < top) {
						vertex(buffer, pose, bx, low, bz - o, 0, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx + 1, low, bz - o, 1, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx + 1, top, bz - o, 1, 0, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx, top, bz - o, 0, 0, RED, GREEN, BLUE, sa);
					}
					if ((low = sideLow(heights[i][j + 1], gy)) < top) {
						vertex(buffer, pose, bx, low, bz + 1 + o, 0, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx + 1, low, bz + 1 + o, 1, 1, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx + 1, top, bz + 1 + o, 1, 0, RED, GREEN, BLUE, sa);
						vertex(buffer, pose, bx, top, bz + 1 + o, 0, 0, RED, GREEN, BLUE, sa);
					}
				}
			}
		});
		poseStack.popPose();
	}

	/** Bottom of a side face next to a neighbour whose ground is at {@code neighbour} (NaN = none in range). */
	private static float sideLow(double neighbour, double top) {
		if (Double.isNaN(neighbour))
			return (float) (top - SIDE_MAX_DROP);
		return (float) Math.max(neighbour, top - SIDE_MAX_DROP);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.isPaused() || MOTES_PER_TICK <= 0 || WitchercraftYrdenTrapMessage.TRAPS.isEmpty())
			return;
		for (WitchercraftYrdenTrapMessage.Trap trap : WitchercraftYrdenTrapMessage.TRAPS.values()) {
			for (int i = 0; i < MOTES_PER_TICK; i++) {
				double a = mc.level.getRandom().nextDouble() * Math.PI * 2;
				double r = Math.sqrt(mc.level.getRandom().nextDouble()) * RADIUS;
				double x = trap.x + Math.cos(a) * r, z = trap.z + Math.sin(a) * r;
				double gy = groundY(mc.level, x, trap.y, z);
				if (!Double.isNaN(gy))
					mc.level.addParticle(MOTE, x, gy + 0.05, z, 0, MOTE_SPEED, 0);
			}
		}
	}

	/** Top of the highest solid block in the column within the search range, or NaN if there is none. */
	private static double groundY(Level level, double x, double y, double z) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int bx = Mth.floor(x), bz = Mth.floor(z), by = Mth.floor(y);
		for (int yy = by + GROUND_SEARCH_UP; yy >= by - GROUND_SEARCH_DOWN; yy--) {
			pos.set(bx, yy, bz);
			BlockState state = level.getBlockState(pos);
			VoxelShape shape = state.getCollisionShape(level, pos);
			if (shape.isEmpty())
				continue;
			pos.set(bx, yy + 1, bz);
			if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
				continue;
			return yy + shape.max(Direction.Axis.Y);
		}
		return Double.NaN;
	}

	private static void pillar(VertexConsumer buffer, PoseStack.Pose pose, float rightX, float rightZ, float alpha) {
		float hw = PILLAR_WIDTH / 2.0F;
		int bottom = alphaByte(PILLAR_ALPHA * alpha);
		vertex(buffer, pose, -rightX * hw, 0, -rightZ * hw, 0, 1, RED, GREEN, BLUE, bottom);
		vertex(buffer, pose, rightX * hw, 0, rightZ * hw, 1, 1, RED, GREEN, BLUE, bottom);
		vertex(buffer, pose, rightX * hw, PILLAR_HEIGHT, rightZ * hw, 1, 0, RED, GREEN, BLUE, 0);
		vertex(buffer, pose, -rightX * hw, PILLAR_HEIGHT, -rightZ * hw, 0, 0, RED, GREEN, BLUE, 0);
	}

	private static void glyph(VertexConsumer buffer, PoseStack.Pose pose, float outX, float outZ, float rightX, float rightZ, float alpha) {
		float h = GLYPH_SIZE / 2.0F;
		int a = alphaByte(GLYPH_ALPHA * alpha);
		if (GLYPH_FLAT) {
			// Texture top (v = 0) towards the outside of the circle, lying on the ground.
			float sideX = -outZ, sideZ = outX;
			float y = GLYPH_LIFT;
			vertex(buffer, pose, -sideX * h + outX * h, y, -sideZ * h + outZ * h, 0, 0, 255, 255, 255, a);
			vertex(buffer, pose, sideX * h + outX * h, y, sideZ * h + outZ * h, 1, 0, 255, 255, 255, a);
			vertex(buffer, pose, sideX * h - outX * h, y, sideZ * h - outZ * h, 1, 1, 255, 255, 255, a);
			vertex(buffer, pose, -sideX * h - outX * h, y, -sideZ * h - outZ * h, 0, 1, 255, 255, 255, a);
		} else {
			float y0 = 0.1F, y1 = 0.1F + GLYPH_SIZE;
			vertex(buffer, pose, -rightX * h, y0, -rightZ * h, 0, 1, 255, 255, 255, a);
			vertex(buffer, pose, rightX * h, y0, rightZ * h, 1, 1, 255, 255, 255, a);
			vertex(buffer, pose, rightX * h, y1, rightZ * h, 1, 0, 255, 255, 255, a);
			vertex(buffer, pose, -rightX * h, y1, -rightZ * h, 0, 0, 255, 255, 255, a);
		}
	}

	private static int alphaByte(float a) {
		return (int) (Mth.clamp(a, 0.0F, 1.0F) * 255.0F);
	}

	private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
		buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
}
