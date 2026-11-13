package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.NativeImage;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.InputStream;

/**
 * Draws the normal Quen glyph orbiting each player WitchercraftQuenGlyphMessage reports as shielded. The glyph is
 * textures/entities/quen_glyph.png pushed out one pixel thick, the way a dropped item is: the PNG's opaque pixels
 * are the 3D shape, so repainting the PNG reshapes it (the shape is read once, a restart picks up a new outline;
 * F3+T alone only updates colours). Drawn every frame from the smoothed player position, so it doesn't lag when
 * moving. Same orbit the old QuenParticles particle used. All tuning lives in the constants below.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftQuenGlyphClient {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/entities/quen_glyph.png");
	private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
	private static final int FULL_BRIGHT = 0xF000F0;

	/** Orbit: distance from the player's centre, height above the feet, bob up and down, radians per tick. */
	private static final float ORBIT_RADIUS = 0.7F;
	private static final float ORBIT_HEIGHT = 1.0F;
	private static final float BOB = 0.3F;
	private static final float ORBIT_SPEED = 0.25F;
	private static final float BOB_SPEED = 0.2F;
	/** Width of the whole 16 px texture in blocks; the thickness is one texture pixel of that. */
	private static final float SIZE = 0.2F;
	/** Brightness of the edges against the faces, so the thickness reads even at full bright. */
	private static final float SIDE_SHADE = 0.65F;

	/** Each vertex: x, y, z, u, v, shade, nx, ny, nz; four per quad. Built from the PNG on first use. */
	private static List<float[]> mesh;

	@SubscribeEvent
	public static void onSubmit(SubmitCustomGeometryEvent event) {
		Map<Integer, Long> glyphs = WitchercraftQuenGlyphMessage.GLYPHS;
		Minecraft mc = Minecraft.getInstance();
		if (glyphs.isEmpty() || mc.level == null)
			return;
		if (mesh == null)
			mesh = buildMesh();
		float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		long gameTime = mc.level.getGameTime();
		Vec3 cam = event.getLevelRenderState().cameraRenderState.pos;
		Iterator<Map.Entry<Integer, Long>> it = glyphs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Long> entry = it.next();
			if (gameTime - entry.getValue() > WitchercraftQuenGlyphMessage.STALE_TICKS) {
				it.remove();
				continue;
			}
			Entity entity = mc.level.getEntity(entry.getKey());
			if (entity == null)
				continue;
			float t = entity.tickCount + partial;
			float angle = t * ORBIT_SPEED;
			Vec3 pos = entity.getPosition(partial).add(ORBIT_RADIUS * Mth.cos(angle), ORBIT_HEIGHT + BOB * Mth.sin(t * BOB_SPEED), ORBIT_RADIUS * Mth.sin(angle));
			PoseStack poseStack = event.getPoseStack();
			poseStack.pushPose();
			poseStack.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
			// upright, turned to face the camera (front face is +z)
			poseStack.mulPose(com.mojang.math.Axis.YP.rotation((float) Mth.atan2(cam.x - pos.x, cam.z - pos.z)));
			event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RENDER_TYPE, WitchercraftQuenGlyphClient::draw);
			poseStack.popPose();
		}
	}

	private static void draw(PoseStack.Pose pose, VertexConsumer buffer) {
		for (float[] v : mesh) {
			int c = (int) (255 * v[5]);
			buffer.addVertex(pose, v[0], v[1], v[2]).setColor(c, c, c, 255).setUv(v[3], v[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, v[6], v[7], v[8]);
		}
	}

	/** Front and back are one full-texture quad each (the render type cuts out clear pixels); every opaque pixel next to a clear one gets an edge face. */
	private static List<float[]> buildMesh() {
		List<float[]> out = new ArrayList<>();
		float h = SIZE / 2.0F, d = SIZE / 32.0F;
		quad(out, new float[][] { { -h, -h, d, 0, 1 }, { h, -h, d, 1, 1 }, { h, h, d, 1, 0 }, { -h, h, d, 0, 0 } }, 1.0F, 0, 0, 1);
		quad(out, new float[][] { { h, -h, -d, 1, 1 }, { -h, -h, -d, 0, 1 }, { -h, h, -d, 0, 0 }, { h, h, -d, 1, 0 } }, 1.0F, 0, 0, -1);
		boolean[][] solid = readAlpha();
		if (solid == null)
			return out;
		int w = solid.length, hgt = solid[0].length;
		float px = SIZE / w, py = SIZE / hgt;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < hgt; y++) {
				if (!solid[x][y])
					continue;
				float x0 = -h + x * px, x1 = x0 + px, y1 = h - y * py, y0 = y1 - py;
				float u = (x + 0.5F) / w, v = (y + 0.5F) / hgt;
				if (y == 0 || !solid[x][y - 1])
					quad(out, new float[][] { { x0, y1, d, u, v }, { x1, y1, d, u, v }, { x1, y1, -d, u, v }, { x0, y1, -d, u, v } }, SIDE_SHADE, 0, 1, 0);
				if (y == hgt - 1 || !solid[x][y + 1])
					quad(out, new float[][] { { x0, y0, -d, u, v }, { x1, y0, -d, u, v }, { x1, y0, d, u, v }, { x0, y0, d, u, v } }, SIDE_SHADE, 0, -1, 0);
				if (x == 0 || !solid[x - 1][y])
					quad(out, new float[][] { { x0, y0, -d, u, v }, { x0, y0, d, u, v }, { x0, y1, d, u, v }, { x0, y1, -d, u, v } }, SIDE_SHADE, -1, 0, 0);
				if (x == w - 1 || !solid[x + 1][y])
					quad(out, new float[][] { { x1, y0, d, u, v }, { x1, y0, -d, u, v }, { x1, y1, -d, u, v }, { x1, y1, d, u, v } }, SIDE_SHADE, 1, 0, 0);
			}
		}
		return out;
	}

	private static void quad(List<float[]> out, float[][] corners, float shade, float nx, float ny, float nz) {
		for (float[] c : corners)
			out.add(new float[] { c[0], c[1], c[2], c[3], c[4], shade, nx, ny, nz });
	}

	/** [x][y] = pixel is opaque enough to survive the render type's cutout; null if the PNG can't be read. */
	private static boolean[][] readAlpha() {
		Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(TEXTURE);
		if (res.isEmpty())
			return null;
		try (InputStream in = res.get().open(); NativeImage img = NativeImage.read(in)) {
			boolean[][] solid = new boolean[img.getWidth()][img.getHeight()];
			for (int x = 0; x < img.getWidth(); x++)
				for (int y = 0; y < img.getHeight(); y++)
					solid[x][y] = (img.getPixel(x, y) >>> 24) > 25;
			return solid;
		} catch (Exception e) {
			return null;
		}
	}
}
