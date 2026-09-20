package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Player-centered HUD view over the same authorized terrain cache used by the full-screen world map.
 */
@EventBusSubscriber(Dist.CLIENT)
public final class WorldMapMinimapHud {
	private static final int MARGIN = 8;
	private static final int BACKGROUND = 0xD0181714;
	private static final int TEXT = 0xFFE8DDBE;
	private static final int WAYPOINT_BASE_SIZE = 14;
	private static final int TRACKING_BASE_SIZE = 16;
	private static final Identifier PLAYER_MARKER = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/map_player_arrow.png");
	private static final Identifier SQUARE_FRAME = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/minimap_frame_square.png");
	private static final Identifier TRACKING_MARKER = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/map_tracking_marker.png");

	private WorldMapMinimapHud() {}

	@SubscribeEvent
	public static void render(RenderGuiEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!shouldRender(minecraft))
			return;

		GuiGraphicsExtractor graphics = event.getGuiGraphics();
		int size = Math.min(WorldMapClientConfig.minimapSize(), Math.min(graphics.guiWidth() - 2 * MARGIN, graphics.guiHeight() - 2 * MARGIN));
		if (size < 32)
			return;

		int x = switch (WorldMapClientConfig.minimapCorner()) {
			case TOP_LEFT, BOTTOM_LEFT -> MARGIN;
			case TOP_RIGHT, BOTTOM_RIGHT -> graphics.guiWidth() - MARGIN - size;
		};
		int y = switch (WorldMapClientConfig.minimapCorner()) {
			case TOP_LEFT, TOP_RIGHT -> MARGIN;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> graphics.guiHeight() - MARGIN - size;
		};
		int inset = Math.min((int)Math.round(size * WorldMapClientConfig.minimapViewportInset() / 64.0), (size - 32) / 2);
		int viewportX = x + inset;
		int viewportY = y + inset;
		int viewportSize = size - inset * 2;

		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		float yaw = minecraft.player.getViewYRot(partialTick);
		double centerX = Mth.lerp(partialTick, minecraft.player.xo, minecraft.player.getX());
		double centerZ = Mth.lerp(partialTick, minecraft.player.zo, minecraft.player.getZ());
		double rotation = WorldMapClientConfig.minimapRotation() ? 180.0 - yaw : 0.0;

		WorldMapWaypointClientCache.requestSnapshotIfNeeded();
		graphics.fill(viewportX, viewportY, viewportX + viewportSize, viewportY + viewportSize, BACKGROUND);
		graphics.enableScissor(viewportX, viewportY, viewportX + viewportSize, viewportY + viewportSize);
		WorldMapClientTileCache.renderAndRequest(graphics, viewportX, viewportY, viewportSize, viewportSize, centerX, centerZ, WorldMapClientConfig.minimapZoom(), rotation);
		drawWaypoints(graphics, minecraft.player.level().dimension().identifier(), viewportX, viewportY, viewportSize, centerX, centerZ, rotation);
		drawTrackingTarget(graphics, minecraft.player.level().dimension().identifier(), viewportX, viewportY, viewportSize, centerX, centerZ, rotation);
		graphics.disableScissor();
		drawSquareFrame(graphics, x, y, size);
		drawNorth(graphics, minecraft.font, viewportX, viewportY, viewportSize, yaw);
		drawPlayer(graphics, x + size / 2.0f, y + size / 2.0f, size, yaw);
	}

	private static boolean shouldRender(Minecraft minecraft) {
		return minecraft.player != null && minecraft.level != null && minecraft.screen == null && !minecraft.options.hideGui
			&& minecraft.player.level().dimension().equals(Level.OVERWORLD)
			&& WorldMapClientConfig.minimapEnabled() && WorldMapServerConfig.minimapEnabled();
	}

	private static void drawSquareFrame(GuiGraphicsExtractor graphics, int x, int y, int size) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SQUARE_FRAME, x, y, 0.0F, 0.0F, size, size, 64, 64, 64, 64);
	}

	private static void drawWaypoints(GuiGraphicsExtractor graphics, Identifier dimension, int x, int y, int size,
			double centerX, double centerZ, double rotationDegrees) {
		if (!WorldMapPoiFilterPreferences.personalWaypoints(WorldMapPoiClientCache.worldId()))
			return;
		int markerSize = Math.max(8, (int)Math.round(WAYPOINT_BASE_SIZE * WorldMapClientConfig.markerScale()));
		double half = size / 2.0;
		double radians = Math.toRadians(rotationDegrees);
		double cosine = Math.cos(radians);
		double sine = Math.sin(radians);
		double zoom = WorldMapClientConfig.minimapZoom();
		for (WorldMapWaypoints.Waypoint waypoint : WorldMapWaypointClientCache.waypoints(dimension)) {
			if (!waypoint.visible())
				continue;
			double scaledX = (waypoint.x() - centerX) * zoom;
			double scaledZ = (waypoint.z() - centerZ) * zoom;
			double positionX = cosine * scaledX - sine * scaledZ;
			double positionY = sine * scaledX + cosine * scaledZ;
			if (Math.abs(positionX) > half - markerSize / 2.0 || Math.abs(positionY) > half - markerSize / 2.0)
				continue;
			int markerX = (int)Math.round(x + half + positionX - markerSize / 2.0);
			int markerY = (int)Math.round(y + half + positionY - markerSize / 2.0);
			MapPage.drawWaypointIcon(graphics, waypoint.icon().atlasIndex(), markerX, markerY, markerSize, MapLayout.WAYPOINT_COLOR);
		}
	}

	private static void drawTrackingTarget(GuiGraphicsExtractor graphics, Identifier dimension, int x, int y, int size,
			double centerX, double centerZ, double rotationDegrees) {
		WorldMapWaypointClientCache.TemporaryPin target = WorldMapWaypointClientCache.temporaryPin(dimension);
		if (target == null)
			return;
		int markerSize = Math.max(8, (int)Math.round(TRACKING_BASE_SIZE * WorldMapClientConfig.markerScale()));
		double radians = Math.toRadians(rotationDegrees);
		double cosine = Math.cos(radians);
		double sine = Math.sin(radians);
		double scaledX = (target.x() - centerX) * WorldMapClientConfig.minimapZoom();
		double scaledZ = (target.z() - centerZ) * WorldMapClientConfig.minimapZoom();
		double positionX = cosine * scaledX - sine * scaledZ;
		double positionY = sine * scaledX + cosine * scaledZ;
		double limit = size / 2.0 - markerSize / 2.0 - 1.0;
		boolean outside = Math.abs(positionX) > limit || Math.abs(positionY) > limit;
		if (outside) {
			double scale = Math.min(positionX == 0.0 ? Double.POSITIVE_INFINITY : limit / Math.abs(positionX),
				positionY == 0.0 ? Double.POSITIVE_INFINITY : limit / Math.abs(positionY));
			positionX *= scale;
			positionY *= scale;
		}
		float markerX = (float)(x + size / 2.0 + positionX);
		float markerY = (float)(y + size / 2.0 + positionY);
		graphics.pose().pushMatrix();
		graphics.pose().translate(markerX, markerY);
		if (outside)
			graphics.pose().rotate((float)Math.atan2(positionX, -positionY));
		graphics.blit(RenderPipelines.GUI_TEXTURED, TRACKING_MARKER, -markerSize / 2, -markerSize / 2, 0.0F, 0.0F,
			markerSize, markerSize, 16, 16, 16, 16);
		graphics.pose().popMatrix();
	}

	private static void drawNorth(GuiGraphicsExtractor graphics, Font font, int x, int y, int size, float yaw) {
		double angle = WorldMapClientConfig.minimapRotation() ? Math.toRadians(180.0 - yaw) : 0.0;
		double dx = Math.sin(angle);
		double dy = -Math.cos(angle);
		double radius = size / 2.0 - 10.0;
		double edgeScale = Math.max(Math.abs(dx), Math.abs(dy));
		if (edgeScale > 0.0)
			radius /= edgeScale;
		int labelX = (int)Math.round(x + size / 2.0 + dx * radius - font.width("N") / 2.0);
		int labelY = (int)Math.round(y + size / 2.0 + dy * radius - font.lineHeight / 2.0);
		graphics.text(font, "N", labelX, labelY, TEXT, false);
	}

	private static void drawPlayer(GuiGraphicsExtractor graphics, float centerX, float centerY, int mapSize, float yaw) {
		int markerSize = Math.max(10, Math.min(18, mapSize / 7));
		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		graphics.pose().rotate((float)Math.toRadians(WorldMapClientConfig.minimapRotation() ? 180.0 : yaw));
		graphics.blit(RenderPipelines.GUI_TEXTURED, PLAYER_MARKER, -markerSize / 2, -markerSize / 2, 0.0F, 0.0F,
			markerSize, markerSize, 64, 64, 64, 64);
		graphics.pose().popMatrix();
	}
}
